package server.motion.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.motion.model.MotionMessage;
import server.motion.service.ActorMotionService;

@Slf4j
@KafkaListener(
        groupId = "multi-player-motion-listener-group",
        uniqueGroupId = true, // processed by each node
        offsetReset = OffsetReset.LATEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "multi-player-motion-listener-client")
// Should be received by all nodes - requires change in the group id
public class MultiPlayerMotionUpdateListener {

    @Inject ActorMotionService actorMotionService;

    @Topic("force-update-actor-motion-update")
    void sendForceUpdateActorMotion(MotionMessage motionMessage) {
        actorMotionService.handleRelayActorMotion(motionMessage);
    }

    @Topic("player-motion-update-result")
    void receivePlayerMotionUpdate(PlayerMotion playerMotion) {
        actorMotionService.handleRelayPlayerMotion(playerMotion);
    }

    @Topic("mob-motion-update-result")
    void receiveMobMotionUpdate(Monster monster) {
        actorMotionService.handleRelayMobMotion(monster);
    }
}
