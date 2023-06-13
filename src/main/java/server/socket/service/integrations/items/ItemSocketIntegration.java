package server.socket.service.integrations.items;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.inventory.service.InventoryService;
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

    public void handleDropItem(GenericInventoryData request, WebSocketSession session) {
        inventoryService
                .dropItem(
                        request.getCharacterName(),
                        request.getItemInventoryLocation(),
                        request.getLocation())
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
                            updateProducer.addItemToMap(droppedItem);

                            inventoryService
                                    .getInventory(request.getCharacterName())
                                    .doOnSuccess(
                                            inventory -> {
                                                GenericInventoryData inventoryData =
                                                        new GenericInventoryData();
                                                inventoryData.setInventory(inventory);
                                                SocketResponse response =
                                                        SocketResponse.builder()
                                                                .messageType(
                                                                        SocketResponseType
                                                                                .INVENTORY_UPDATE
                                                                                .getType())
                                                                .inventoryData(inventoryData)
                                                                .build();
                                                session.send(response)
                                                        .subscribe(socketResponseSubscriber);
                                            })
                                    .subscribe();
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

                            GenericInventoryData inventoryData = new GenericInventoryData();
                            inventoryData.setInventory(inventory);
                            SocketResponse response =
                                    SocketResponse.builder()
                                            .messageType(
                                                    SocketResponseType.INVENTORY_UPDATE.getType())
                                            .inventoryData(inventoryData)
                                            .build();
                            session.send(response).subscribe(socketResponseSubscriber);
                        })
                .subscribe();
    }
}
