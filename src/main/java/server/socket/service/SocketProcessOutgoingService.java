package server.socket.service;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.security.InvalidParameterException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import server.motion.dto.PlayerMotion;
import server.socket.model.MessageType;
import server.socket.model.SocketMessage;
import server.socket.producer.UpdateProducer;
import server.socket.service.integrations.items.ItemSocketIntegration;

@Slf4j
@Singleton
public class SocketProcessOutgoingService {
    // this service determines what happens with the outgoing message - specifically where it gets
    // sent

    @Inject UpdateProducer updateProducer;

    @Inject ItemSocketIntegration itemSocketIntegration;

    public void processMessage(SocketMessage socketMessage, WebSocketSession session) {
        String updateType = socketMessage.getUpdateType();

        if (updateType == null) {
            throw new InvalidParameterException("message type missing");
        }

        // TODO: Make this more pretty
        if (updateType.equals(MessageType.PLAYER_MOTION.getType())) {
            handlePlayerMotionUpdate(socketMessage);
        } else if (updateType.equals(MessageType.CREATE_MOB.getType())) {
            handleCreateMob(socketMessage);
        } else if (updateType.equals(MessageType.MOB_MOTION.getType())) {
            handleMobMotionUpdate(socketMessage);
        } else if (updateType.equals(MessageType.PLAYER_COMBAT.getType())) {
            handlePlayerCombatAction(socketMessage);
        } else if (updateType.equals(MessageType.MOB_COMBAT.getType())) {
            handleMobCombatAction(socketMessage);
        } else if (updateType.equals(MessageType.PICKUP_ITEM.getType())) {
            handlePickupItem(socketMessage, session);
        } else if (updateType.equals(MessageType.DROP_ITEM.getType())) {
            handleDropItem(socketMessage, session);
        } else if (updateType.equals(MessageType.FETCH_INVENTORY.getType())) {
            handleFetchInventory(socketMessage, session);
        } else {
            log.error("Did not recognise update type, {}", updateType);
        }
    }

    // update motion for player
    private void handlePlayerMotionUpdate(SocketMessage message) {
        PlayerMotion motion = message.getPlayerMotion();

        Map<String, String> validateFields =
                Map.of(
                        motion.getPlayerName(),
                        "Player name",
                        "i",
                        "b",
                        motion.getMotion().getMap(),
                        "Map",
                        motion.getMotion().getX().toString(),
                        "X co-ordinate",
                        motion.getMotion().getY().toString(),
                        "Y co-ordinate",
                        motion.getMotion().getZ().toString(),
                        "Z co-ordinate");

        for (Map.Entry<String, String> entry : validateFields.entrySet()) {
            if (!validate(entry.getValue(), entry.getKey())) {
                return;
            }
        }

        updateProducer.sendPlayerMotionUpdate(message.getPlayerMotion());
    }

    private boolean validate(String value, String name) {
        if (!isValid(value)) {
            log.error("{} is not valid in player motion!", name);
            return false;
        }
        return true;
    }

    private boolean isValid(String data) {
        return data != null && !data.isBlank();
    }

    // update motion for monster
    private void handleMobMotionUpdate(SocketMessage message) {
        updateProducer.sendMobMotionUpdate(message.getMonster());
    }

    private void handleCreateMob(SocketMessage message) {
        updateProducer.sendCreateMob(message.getMonster());
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
    private void handlePickupItem(SocketMessage message, WebSocketSession session) {
        itemSocketIntegration.handlePickupItem(message.getInventoryRequest(), session);
    }

    private void handleDropItem(SocketMessage message, WebSocketSession session) {
        itemSocketIntegration.handleDropItem(message.getInventoryRequest(), session);
    }

    private void handleFetchInventory(SocketMessage message, WebSocketSession session) {
        itemSocketIntegration.handleFetchInventory(message.getInventoryRequest(), session);
    }
}
