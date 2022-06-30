package server.player.character.inventory.controller;

import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import server.items.dropped.model.DroppedItem;
import server.items.dropped.model.DroppedItemDto;
import server.player.character.inventory.model.GenericInventoryData;
import server.player.character.inventory.model.Inventory;
import server.player.character.inventory.service.InventoryService;

import javax.inject.Inject;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/v1/inventory")
public class InventoryController {

    @Inject
    InventoryService inventoryService;

    @Post("/pickup")
    public Inventory pickupItem(@Body GenericInventoryData pickupRequest, @Header String characterName) {
        return inventoryService.pickupItem(characterName, pickupRequest.getDroppedItemId());
    }

    @Post("/drop")
    public DroppedItemDto dropItem(@Body GenericInventoryData dropRequest, @Header String characterName) {
        return inventoryService.dropItem(characterName,
                dropRequest.getItemInventoryLocation(), dropRequest.getLocation());
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
