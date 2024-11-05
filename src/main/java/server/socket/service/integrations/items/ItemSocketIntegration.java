package server.socket.service.integrations.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location2D;
import server.items.equippable.service.EquipItemService;
import server.items.inventory.model.Inventory;
import server.items.inventory.model.ItemInstanceIds;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.inventory.service.InventoryService;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;
import server.socket.producer.UpdateProducer;

@Slf4j
@Singleton
public class ItemSocketIntegration {
    // some functions will require to be processed serially, like picking up items
    // we should validate request, such as inventory has space and provide result before we can
    // pickup item
    // here we can have access to session, so we can provide results back before publishing changes

    @Inject InventoryService inventoryService;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    @Inject UpdateProducer updateProducer;

    @Inject EquipItemService equipItemService;

    @Inject SessionParamHelper sessionParamHelper;

    ObjectMapper objectMapper = new ObjectMapper();

    public void handleDropItem(GenericInventoryData request, WebSocketSession session) {
        inventoryService
                .dropItem(request.getActorId(), request.getItemInstanceId(), request.getLocation())
                .doOnError(
                        e -> {
                            log.warn(
                                    "Failed to pickup item, {}",
                                    e.getMessage()); // could be inventory full
                            SocketResponse response =
                                    SocketResponse.builder()
                                            .messageType(
                                                    SocketResponseType.INVENTORY_ERROR.getType())
                                            .error(e.getMessage())
                                            .build();
                            session.send(response).subscribe(socketResponseSubscriber);
                        })
                .doOnSuccess(
                        droppedItem -> {
                            log.info(
                                    "Dropped item instance created, returning inventory to player"
                                            + " and sending item to be broadcasted");
                            sendInventoryToPlayer(session, request.getActorId());
                            updateProducer.addItemToMap(droppedItem);
                        })
                .subscribe();
    }

    public void handlePickupItem(GenericInventoryData request, WebSocketSession session) {
        inventoryService
                .pickupItem(request)
                .doOnError(
                        e -> {
                            log.warn(
                                    "Failed to pickup item, {}",
                                    e.getMessage()); // could be inventory full
                            SocketResponse response =
                                    SocketResponse.builder()
                                            .messageType(
                                                    SocketResponseType.INVENTORY_ERROR.getType())
                                            .error(e.getMessage())
                                            .build();
                            session.send(response).subscribe(socketResponseSubscriber);
                        })
                .doOnSuccess(
                        inventory -> {
                            updateProducer.removeItemFromMap(request.getItemInstanceId());

                            sendInventory(inventory, session);
                        })
                .subscribe();
    }

    public void handleFetchInventory(GenericInventoryData request, WebSocketSession session) {
        sendInventoryToPlayer(session, request.getActorId());
    }

    public void handleFetchEquipped(GenericInventoryData request, WebSocketSession session) {
        equipItemService
                .getEquippedItems(request.getActorId())
                .doOnSuccess(
                        equippedItems -> {
                            if (equippedItems.isEmpty()) {
                                return;
                            }
                            GenericInventoryData inventoryData = new GenericInventoryData();
                            inventoryData.setActorId(equippedItems.get(0).getActorId());
                            inventoryData.setEquippedItems(equippedItems);
                            SocketResponse response =
                                    SocketResponse.builder()
                                            .messageType(
                                                    SocketResponseType.ADD_EQUIP_ITEM.getType())
                                            .inventoryData(inventoryData)
                                            .build();
                            session.send(response).subscribe(socketResponseSubscriber);
                        })
                .subscribe();
    }

    public void handleEquipItem(GenericInventoryData request, WebSocketSession session) {
        equipItemService
                .equipItem(request.getItemInstanceId(), request.getActorId())
                .doOnError(e -> log.error("Failed to equip item, {}", e.getMessage()))
                .doOnSuccess(
                        equippedItems -> {
                            sendInventoryToPlayer(session, request.getActorId());

                            GenericInventoryData equipData = new GenericInventoryData();
                            equipData.setEquippedItems(List.of(equippedItems));
                            equipData.setActorId(equippedItems.getActorId());
                            updateProducer.notifyEquipItems(equippedItems); // notify other players
                        })
                .subscribe();
    }

    public void handleUnEquipItem(GenericInventoryData request, WebSocketSession session) {
        equipItemService
                .unEquipItem(request.getItemInstanceId(), request.getActorId())
                .doOnError(e -> log.error("Failed to un-equip item, {}", e.getMessage()))
                .doOnSuccess(
                        unequippedItemInstanceId -> {
                            sendInventoryToPlayer(session, request.getActorId());

                            GenericInventoryData equipData = new GenericInventoryData();
                            equipData.setItemInstanceIds(List.of(unequippedItemInstanceId));
                            equipData.setActorId(request.getActorId());

                            SocketResponse res =
                                    SocketResponse.builder()
                                            .inventoryData(equipData)
                                            .itemInstanceIds(Set.of(unequippedItemInstanceId))
                                            .messageType(
                                                    SocketResponseType.REMOVE_EQUIP_ITEM.getType())
                                            .build();

                            session.send(res).subscribe(socketResponseSubscriber);
                            ItemInstanceIds itemInstanceIds =
                                    ItemInstanceIds.builder()
                                            .itemInstanceIds(List.of(unequippedItemInstanceId))
                                            .actorId(request.getActorId())
                                            .build();
                            updateProducer.notifyUnEquipItems(
                                    itemInstanceIds); // notify other players
                        })
                .subscribe();
    }

    private void sendInventoryToPlayer(WebSocketSession session, String actorId) {
        inventoryService
                .getInventory(actorId)
                .doOnError(e -> log.error("Failed to fetch inventory, {}", e.getMessage()))
                .doOnSuccess(inventory -> sendInventory(inventory, session))
                .subscribe();
    }

    private void sendInventory(Inventory inventory, WebSocketSession session) {
        GenericInventoryData inventoryData = new GenericInventoryData();
        inventoryData.setInventory(inventory);

        SocketResponse res =
                SocketResponse.builder()
                        .inventoryData(inventoryData)
                        .messageType(SocketResponseType.INVENTORY_UPDATE.getType())
                        .build();

        session.send(res).subscribe(socketResponseSubscriber);
    }

    public void handleMoveItem(GenericInventoryData request, WebSocketSession session) {
        String itemInstanceId = request.getItemInstanceId();
        Location2D to = request.getTo();
        String category = request.getCategory();

        if (to != null) {
            inventoryService
                    .moveItem(SessionParamHelper.getActorId(session), itemInstanceId, to)
                    .doOnSuccess(inventory -> sendInventory(inventory, session))
                    .subscribe();
        }
    }
}
