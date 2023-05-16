package server.motion.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.motion.dto.PlayerMotion;

@KafkaClient(id = "player-motion-client")
public interface PlayerMotionUpdateProducer {

    @Topic("player-motion-update-result")
    void sendPlayerMotionResult(PlayerMotion playerMotion);
}
