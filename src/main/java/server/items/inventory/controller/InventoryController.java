package server.items.inventory.controller;

import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import server.items.inventory.model.Inventory;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.model.DroppedItem;
import server.items.inventory.service.InventoryService;

@Controller("/v1/inventory")
public class InventoryController {

    @Inject InventoryService inventoryService;

    @Post("/pickup")
    public Inventory pickupItem(
            @Body GenericInventoryData pickupRequest, @Header String characterName) {
        return inventoryService.pickupItem(characterName, pickupRequest.getDroppedItemId());
    }

    @Post("/drop")
    public DroppedItem dropItem(
            @Body GenericInventoryData dropRequest, @Header String characterName) {
        return inventoryService.dropItem(
                characterName, dropRequest.getItemInventoryLocation(), dropRequest.getLocation());
    }

    @Get
    public Inventory getInventory(@Header String characterName) {
        return inventoryService.getInventory(characterName);
    }

    @Post("/generate-inventory")
    public Inventory createInventoryForCharacter(@Body GenericInventoryData data) {
        // This is test endpoint, this will be connected to the character creation process.

        return inventoryService.createInventoryForNewCharacter(data.getCharacterName());
    }

    @Post("/clear-data")
    public void clearAllData(@Header String characterName) {
        // this is a test endpoint

        inventoryService.clearAllDataForCharacter(characterName);
    }
}
