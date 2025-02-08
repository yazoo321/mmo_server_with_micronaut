package server.motion.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.motion.dto.PlayerMotion;
import server.motion.model.MotionMessage;

@KafkaClient(id = "player-motion-client")
public interface PlayerMotionUpdateProducer {

    @Topic("player-motion-update-result")
    void sendPlayerMotionResult(PlayerMotion playerMotion);

    // TODO: we need to somehow receive messages based on relevant map data.
    // this is a complex problem, we have no guarantee which node processes characters on which map
    @Topic("force-update-actor-motion-update")
    void sendForceUpdateActorMotion(MotionMessage motionMessage);

}
