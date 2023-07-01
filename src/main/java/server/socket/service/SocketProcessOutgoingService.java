package server.socket.service;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.function.BiConsumer;
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

    Map<String, BiConsumer<SocketMessage, WebSocketSession>> functionMap =
            Map.of(
                    MessageType.PLAYER_MOTION.getType(), this::handlePlayerMotionUpdate,
                    MessageType.CREATE_MOB.getType(), this::handleCreateMob,
                    MessageType.MOB_MOTION.getType(), this::handleMobMotionUpdate,
                    MessageType.PICKUP_ITEM.getType(), this::handlePickupItem,
                    MessageType.DROP_ITEM.getType(), this::handleDropItem,
                    MessageType.FETCH_INVENTORY.getType(), this::handleFetchInventory,
                    MessageType.EQUIP_ITEM.getType(), this::handleEquipItem);

    public void processMessage(SocketMessage socketMessage, WebSocketSession session) {
        String updateType = socketMessage.getUpdateType();

        if (updateType == null) {
            throw new InvalidParameterException("message type missing");
        }

        if (functionMap.containsKey(updateType)) {
            functionMap.get(updateType).accept(socketMessage, session);
        } else {
            log.error("Did not recognise update type, {}", updateType);
        }
    }

    // update motion for player
    private void handlePlayerMotionUpdate(SocketMessage message, WebSocketSession session) {
        PlayerMotion motion = message.getPlayerMotion();

        Map<String, String> validateFields =
                Map.of(
                        "Player name",
                        motion.getPlayerName(),
                        "b",
                        "i",
                        "Map",
                        motion.getMotion().getMap(),
                        "X co-ordinate",
                        motion.getMotion().getX().toString(),
                        "Y co-ordinate",
                        motion.getMotion().getY().toString(),
                        "Z co-ordinate",
                        motion.getMotion().getZ().toString()
                );

        for (Map.Entry<String, String> entry : validateFields.entrySet()) {
            if (!validate(entry.getKey(), entry.getValue())) {
                return;
            }
        }

        updateProducer.sendPlayerMotionUpdate(message.getPlayerMotion());
    }

    // update motion for monster
    private void handleMobMotionUpdate(SocketMessage message, WebSocketSession session) {
        updateProducer.sendMobMotionUpdate(message.getMonster());
    }

    private void handleCreateMob(SocketMessage message, WebSocketSession session) {
        updateProducer.sendCreateMob(message.getMonster());
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

    private void handleEquipItem(SocketMessage message, WebSocketSession session) {
        itemSocketIntegration.handleEquipItem(message.getInventoryRequest(), session);
    }

    private void handleUnEquipItem(SocketMessage message, WebSocketSession session) {}

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
}
