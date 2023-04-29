package server.motion.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.motion.dto.PlayerMotion;
import server.motion.service.PlayerMotionService;

@Slf4j
@KafkaListener(groupId = "mmo-server", offsetReset = OffsetReset.LATEST)
public class PlayerMotionUpdateListener {

    @Inject PlayerMotionService playerMotionService;

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
    }
}
