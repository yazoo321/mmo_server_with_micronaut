package server.player.character.inventory.controller;

import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import server.items.dropped.model.DroppedItem;
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
        // TODO: add authentication and verify user

        return inventoryService.pickupItem(characterName, pickupRequest.getDroppedItem());
    }

    @Post("/drop")
    public DroppedItem dropItem(@Body GenericInventoryData dropRequest, @Header String characterName) {

        return inventoryService.dropItem(characterName,
                dropRequest.getCharacterItem(), dropRequest.getLocation());
    }

    @Get
    public Inventory getInventory(@Header String characterName) {
        // TODO: introduce header as jwt token or similar to get character name

        return inventoryService.getInventory(characterName);
    }
}
