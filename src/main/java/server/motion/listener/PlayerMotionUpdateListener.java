package server.motion.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.motion.dto.PlayerMotion;
import server.motion.service.PlayerMotionService;
import server.session.SessionParamHelper;

@Slf4j
@KafkaListener(
        groupId = "mmo-server",
        offsetReset = OffsetReset.EARLIEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "player_motion_client")
public class PlayerMotionUpdateListener {

    @Inject PlayerMotionService playerMotionService;

    @Inject
    SessionParamHelper sessionParamHelper;

    @Topic("player-motion-update")
    public void receive(PlayerMotion playerMotion) {
        // TODO: validate
        playerMotionService
                .updatePlayerMotion(playerMotion)
                .doOnError(
                        (error) ->
                                log.error("Error updating player motion, {}", error.getMessage()))
                .subscribe();

        // make others aware of this motion
        playerMotionService.relayPlayerMotion(playerMotion);
        sessionParamHelper.setSharedActorMotion(playerMotion.getActorId(), playerMotion.getMotion());
    }
}
