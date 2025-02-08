package server.socket.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.items.equippable.model.EquippedItems;
import server.items.inventory.model.ItemInstanceIds;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.model.DroppedItem;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.service.WebsocketClientUpdatesService;

import java.util.List;

@Slf4j
@KafkaListener(
        groupId = "socket-listener",
        offsetReset = OffsetReset.LATEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "socket-listener")
public class SocketUpdateListener {

    @Inject WebsocketClientUpdatesService websocketClientUpdatesService;

    @Topic("item-added-to-map")
    void itemAddedToMap(DroppedItem droppedItem) {
        websocketClientUpdatesService.sendDroppedItemUpdates(droppedItem);
    }

    @Topic("item-removed-from-map")
    void itemRemovedFromMap(String itemInstanceId) {
        websocketClientUpdatesService.sendItemPickupUpdates(itemInstanceId);
    }

    @Topic("notify-equip-items")
    void notifyItemEquip(EquippedItems equippedItems) {
        if (equippedItems == null) {
            return;
        }
        String actorId = equippedItems.getActorId();
        GenericInventoryData equipData = new GenericInventoryData();
        equipData.setActorId(actorId);
        equipData.setEquippedItems(List.of(equippedItems));
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .inventoryData(equipData)
                        .messageType(SocketResponseType.ADD_EQUIP_ITEM.getType())
                        .build();

        websocketClientUpdatesService.sendUpdateToListeningIncludingSelf(socketResponse, actorId);
    }

    @Topic("notify-un-equip-items")
    void notifyUnEquipItem(ItemInstanceIds itemInstanceIds) {

        if (itemInstanceIds == null
                || itemInstanceIds.getItemInstanceIds() == null
                || itemInstanceIds.getItemInstanceIds().isEmpty()) {
            return;
        }
        GenericInventoryData equipData = new GenericInventoryData();
        equipData.setActorId(itemInstanceIds.getActorId());
        equipData.setItemInstanceIds(itemInstanceIds.getItemInstanceIds());
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .inventoryData(equipData)
                        .messageType(SocketResponseType.REMOVE_EQUIP_ITEM.getType())
                        .build();

        websocketClientUpdatesService.sendUpdateToListeningIncludingSelf(
                socketResponse, itemInstanceIds.getActorId());
    }
}
