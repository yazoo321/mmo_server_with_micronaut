package server.attribute.stats.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeEffects;
import server.attribute.stats.model.DamageSource;
import server.attribute.stats.model.DamageUpdateMessage;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.PlayerLevelStatsService;
import server.attribute.stats.service.StatsService;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.service.WebsocketClientUpdatesService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@KafkaListener(
        groupId = "stats-listener",
        offsetReset = OffsetReset.LATEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "stats-listener")
public class StatsListener {

    @Inject WebsocketClientUpdatesService clientUpdatesService;

    @Inject StatsService statsService;

    @Inject
    PlayerLevelStatsService playerLevelStatsService;

    @Topic("update-actor-stats")
    public void receiveUpdatePlayerAttributes(Stats stats) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.STATS_UPDATE.getType())
                        .stats(stats)
                        .build();

        clientUpdatesService.sendUpdateToListeningIncludingSelf(socketResponse, stats.getActorId());
    }

    @Topic("processed-damage-updates")
    public void receiveDamageUpdates(DamageUpdateMessage damageUpdateMessage) {
        log.info("Received processed-damage-updates message: {}", damageUpdateMessage);
        // relay this to the clients
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.DAMAGE_UPDATE.getType())
                        .damageSource(damageUpdateMessage.getDamageSource())
                        .build();

        clientUpdatesService.sendUpdateToListeningIncludingSelf(
                socketResponse, damageUpdateMessage.getOriginStats().getActorId());
    }

    @Topic("notify-actor-death")
    void receive_actor_death_notify(DamageUpdateMessage damageUpdateMessage) {
        if (damageUpdateMessage.getOriginStats().isPlayer()) {
            playerLevelStatsService.handleAddXp(
                    damageUpdateMessage.getTargetStats(),
                    damageUpdateMessage.getOriginStats());
        }
    }

    @Topic("request-take-damage")
    public void requestTakeDamage(DamageSource damageSource) {
        log.info("request to take damage received! {}", damageSource);
        statsService.takeDamage(
                damageSource.getActorId(),
                damageSource.getDamageMap(),
                damageSource.getSourceActorId());
    }

    @Topic("update-actor-status")
    public void receive_actor_statuses(ActorStatus actorStatus) {
        log.info("stats service received status update {}", actorStatus);

        Map<String, AttributeEffects> statusesAffectingStats = new HashMap<>();

        for (Status status : actorStatus.getActorStatuses()) {
            if (status.requiresStatsUpdate()) {
                merge(statusesAffectingStats, status.getAttributeEffects());
            }
        }

        // TODO: Consider optimisation, do we _need_ to update this? this is triggered when stats
        // update is not required.
        // for example, we can create a separate event for more precise updates, or fetch status now
        // and compare if diff

        statsService
                .getStatsFor(actorStatus.getActorId())
                .doOnSuccess(
                        actorStats -> {
                            actorStats.setStatusEffects(statusesAffectingStats);
                            statsService.evaluateDerivedStats(actorStats);
                        })
                .subscribe();
    }

    private void merge(Map<String, AttributeEffects> left, Map<String, AttributeEffects> right) {
        right.forEach(
                (k, v) -> {
                    if (left.containsKey(k)) {
                        AttributeEffects eff = left.get(k);
                        eff.setAdditiveModifier(
                                eff.getAdditiveModifier() + v.getAdditiveModifier());
                        eff.setMultiplyModifier(
                                eff.getMultiplyModifier() * v.getMultiplyModifier());
                    } else {
                        left.put(k, v);
                    }
                });
    }
}
