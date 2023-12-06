package server.items.inventory.controller;

import io.micronaut.http.annotation.*;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import server.items.inventory.model.Inventory;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.inventory.service.InventoryService;
import server.items.model.DroppedItem;

@Deprecated // use websocket comms instead
@Controller("/v1/inventory")
public class InventoryController {

    @Inject InventoryService inventoryService;

    @Post("/pickup")
    public Single<Inventory> pickupItem(
            @Body GenericInventoryData pickupRequest, @Header String actorId) {
        pickupRequest.setActorId(actorId);
        return inventoryService.pickupItem(pickupRequest);
    }

    @Post("/drop")
    public Single<DroppedItem> dropItem(
            @Body GenericInventoryData dropRequest, @Header String actorId) {
        return inventoryService.dropItem(
                actorId, dropRequest.getItemInstanceId(), dropRequest.getLocation());
    }

    @Get
    public Single<Inventory> getInventory(@Header String actorId) {
        return inventoryService.getInventory(actorId);
    }

    @Post("/generate-inventory")
    public Single<Inventory> createInventoryForCharacter(@Body GenericInventoryData data) {
        // This is test endpoint, this will be connected to the character creation process.

        return inventoryService.createInventoryForNewCharacter(data.getActorId());
    }

    @Post("/clear-data")
    public void clearAllData(@Header String actorId) {
        // this is a test endpoint

        inventoryService.clearAllDataForCharacter(actorId);
    }
}
