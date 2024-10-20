package server.motion.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.service.StatusService;
import server.motion.dto.PlayerMotion;
import server.motion.repository.ActorMotionRepository;
import server.motion.service.PlayerMotionService;

@Slf4j
@KafkaListener(
        groupId = "mmo-server",
        offsetReset = OffsetReset.EARLIEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "player_motion_client")
public class PlayerMotionUpdateListener {

    @Inject PlayerMotionService playerMotionService;

    @Inject ActorMotionRepository actorMotionRepository;

    @Inject
    StatusService statusService;

    @Topic("player-motion-update")
    public void receive(PlayerMotion playerMotion) {
        // TODO: validate
//        log.info("received player motion in player-motion-update");
//        log.info("{}", playerMotion);

        // consider random sampling
        statusService.getActorStatus(playerMotion.getActorId())
                .doOnSuccess(actorStatus -> {
                    if (!actorStatus.canMove()) {
                        log.warn("Actor tried to move whilst dead, {}", playerMotion.getActorId());
                        return;
                    }

                    actorMotionRepository.updateActorMotion(
                            playerMotion.getActorId(), playerMotion.getMotion());
                    playerMotionService.relayPlayerMotion(playerMotion);
                })
                .doOnError(err -> log.error(err.getMessage()))
                .subscribe();

    }
}
