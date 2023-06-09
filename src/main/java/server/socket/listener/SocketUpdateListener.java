package server.socket.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.items.model.DroppedItem;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.socket.service.ClientUpdatesService;

@Slf4j
@KafkaListener(
        groupId = "mmo-server",
        offsetReset = OffsetReset.EARLIEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "socket_listener")
public class SocketUpdateListener {

    @Inject ClientUpdatesService clientUpdatesService;

    @Topic("player-motion-update-result")
    void receivePlayerMotionUpdate(PlayerMotion playerMotion) {
        clientUpdatesService.sendMotionUpdatesToSubscribedClients(playerMotion);
    }

    @Topic("mob-motion-update-result")
    void receiveMobMotionUpdate(Monster monster) {
        clientUpdatesService.sendMotionUpdatesToSubscribedClients(monster);
    }

    @Topic("item-added-to-map")
    void itemAddedToMap(DroppedItem droppedItem) {
        clientUpdatesService.sendDroppedItemUpdates(droppedItem);
    }

    @Topic("item-removed-from-map")
    void itemRemovedFromMap(String itemInstanceId) {
        clientUpdatesService.sendItemPickupUpdates(itemInstanceId);
    }
}
