package server.socket.service;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.socket.model.MessageType;
import server.socket.model.SocketMessage;
import server.socket.producer.UpdateProducer;

@Slf4j
@Singleton
public class SocketProcessOutgoingService {
    // this service determines what happens with the outgoing message - specifically where it gets
    // sent

    UpdateProducer updateProducer;

    public SocketProcessOutgoingService(
            @KafkaClient("update-producer") UpdateProducer updateProducer) {
        // advised this way vs direct inject
        this.updateProducer = updateProducer;
    }

    public void processMessage(SocketMessage socketMessage) {
        String updateType = socketMessage.getUpdateType();

        // TODO: Make this more pretty
        if (updateType.equals(MessageType.PLAYER_MOTION.getType())) {
            handlePlayerMotionUpdate(socketMessage);
        }

        if (updateType.equals(MessageType.MOB_MOTION.getType())) {
            handleMobMotionUpdate(socketMessage);
        }

        if (updateType.equals(MessageType.PLAYER_COMBAT.getType())) {
            handlePlayerCombatAction(socketMessage);
        }

        if (updateType.equals(MessageType.MOB_COMBAT.getType())) {
            handleMobCombatAction(socketMessage);
        }

        if (updateType.equals(MessageType.INVENTORY_UPDATE.getType())) {
            handleInventoryInteraction(socketMessage);
        }

        log.error("Did not recognise update type, {}", updateType);
    }

    // update motion for player
    private void handlePlayerMotionUpdate(SocketMessage message) {
        updateProducer.sendPlayerMotionUpdate(message.getPlayerMotion());
    }

    // update motion for monster
    private void handleMobMotionUpdate(SocketMessage message) {
        updateProducer.sendMobMotionUpdate(message.getMonster());
    }

    // handle player combat action
    private void handlePlayerCombatAction(SocketMessage message) {
        // TODO: TBD
    }

    // handle mob combat action
    private void handleMobCombatAction(SocketMessage message) {
        // TODO: TBD
    }

    // handle inventory interaction
    private void handleInventoryInteraction(SocketMessage message) {
        // TODO: TBD
    }
}
