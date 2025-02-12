package server.monster.server_integration.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.DamageUpdateMessage;
import server.attribute.stats.service.StatsService;
import server.attribute.status.service.StatusService;
import server.common.uuid.UUIDHelper;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.producer.MonsterServerProducer;
import server.monster.server_integration.service.MobInstanceService;
import server.motion.repository.ActorMotionRepository;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.service.WebsocketClientUpdatesService;

@Slf4j
@KafkaListener(
        groupId = "single-mob-listener",
        offsetReset = OffsetReset.LATEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "single-mob-listener")
public class SingleMonsterServerListener {
    // should only be processed on one node

    @Inject MonsterServerProducer monsterServerProducer;

    @Inject MobInstanceService mobInstanceService;

    @Inject StatsService statsService;
    @Inject StatusService statusService;

    @Inject SessionParamHelper sessionParamHelper;

    @Inject ActorMotionRepository actorMotionRepository;

    @Inject WebsocketClientUpdatesService clientUpdatesService;

    @Topic("create-mob")
    public void receiveCreateMob(Monster monster) {
        mobInstanceService
                .createMob(monster)
                .doOnSuccess(
                        mob -> {
                            statsService.initializeMobStats(monster.getActorId());
                            statusService.initializeStatus(monster.getActorId()).subscribe();
                        })
                .doOnError(error -> log.error("Error on creating mob, {}", error.getMessage()))
                .subscribe();
    }

    @Topic("remove-mobs-from-game")
    public void receiveRemoveMobsFromGame(String actorId) {
        notifyClientsToRemoveMobs(actorId);

        sessionParamHelper
                .getLiveSessions()
                .forEach(
                        (actor, session) ->
                                SessionParamHelper.getTrackingMobs(session).remove(actorId));
    }

    private void notifyClientsToRemoveMobs(String actorId) {
        // needs to be delayed
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.REMOVE_MOBS.getType())
                        .lostMobs(Set.of(actorId))
                        .build();
        clientUpdatesService.sendUpdateToListeningIncludingServer(socketResponse, actorId);
    }

    @Topic("mob-motion-update")
    public void receiveUpdateMob(Monster monster) {
        // Add validation
        actorMotionRepository.updateActorMotion(monster.getActorId(), monster.getMotion());
        monsterServerProducer.sendMobUpdateResult(monster);
    }

    @Topic("notify-actor-death")
    void receive_actor_death_notify(DamageUpdateMessage damageUpdateMessage) {
        if (!UUIDHelper.isPlayer(damageUpdateMessage.getTargetStats().getActorId())
                && !UUIDHelper.isPlayer(damageUpdateMessage.getOriginStats().getActorId())) {
            // the target was a mob and the killer is a player
            // TODO: item drops will require to be owned by the killer
            mobInstanceService.handleMobDeath(damageUpdateMessage.getTargetStats());
        }
    }
}
