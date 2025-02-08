package server.motion.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.attribute.status.service.StatusService;
import server.motion.dto.PlayerMotion;
import server.motion.model.MotionMessage;
import server.motion.repository.ActorMotionRepository;
import server.motion.service.ActorMotionService;
import server.motion.service.PlayerMotionService;

@Slf4j
@KafkaListener(
        groupId = "player-motion-listener-group",
        offsetReset = OffsetReset.LATEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "player-motion-listener-client")
// should be processed by a single node
public class SinglePlayerMotionUpdateListener {

    @Inject PlayerMotionService playerMotionService;

    @Inject
    ActorMotionService actorMotionService;

    @Inject ActorMotionRepository actorMotionRepository;

    @Inject StatusService statusService;

    @Topic("player-motion-update")
    public void receive(PlayerMotion playerMotion) {
        // TODO: validate
//        log.info("received player motion in player-motion-update");
//        log.info("{}", playerMotion);

        // TODO: consider random sampling, e.g. 10% of requests (that's still 1+ per second)
        statusService
                .getActorStatus(playerMotion.getActorId())
                .doOnSuccess(
                        actorStatus -> {
                            if (!actorStatus.canMove()) {
                                log.warn(
                                        "Actor tried to move whilst status shouldn't allow, {}",
                                        playerMotion.getActorId());
                                return;
                            }

                            actorMotionRepository.updateActorMotion(
                                    playerMotion.getActorId(), playerMotion.getMotion());
                            playerMotionService.relayPlayerMotion(playerMotion);
                        })
                .doOnError(
                        err ->
                                log.error(
                                        "Error processing player motion update: {}",
                                        err.getMessage()))
                .subscribe();
    }

    @Topic("request-update-motion")
    void requestUpdateMotion(MotionMessage motionMessage) {
        // this should be used by internal skills and similar
        // should not be exposed to the websocket/UDP directly, this should bypass validation
        // should force the update of motion on clients also
        actorMotionService.relayForceUpdateActorMotion(motionMessage);
    }

    @Topic("force-update-actor-motion-update")
    void sendForceUpdateActorMotion(MotionMessage motionMessage) {
        actorMotionService.handleRelayActorMotion(motionMessage);
    }
}
