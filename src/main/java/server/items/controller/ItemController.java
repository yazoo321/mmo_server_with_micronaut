package server.items.controller;

import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import server.common.dto.Location;
import server.items.dropped.model.DroppedItem;
import server.items.dto.Item;
import server.items.service.ItemService;
import server.player.character.inventory.model.GenericInventoryData;

import javax.inject.Inject;
import java.security.Principal;
import java.util.List;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/v1/items")
public class ItemController {

    @Inject
    ItemService itemService;

    @Post("/create-item")
    public Item updatePlayerLocation(Principal principal, @Body GenericInventoryData data) {
        // This is a test endpoint!
        // you should not be creating items live, they should be handled via migration

        Item item = itemService.createItem(data.getItem());

        return item;
    }

    @Post("/spawn-item")
    public DroppedItem spawnItem(Principal principal, @Body GenericInventoryData inventoryData) {
        // TODO: this will be changed to a loot service. e.g. when mob is killed, spawn random items
        return itemService.dropItem(inventoryData.getItemId(), inventoryData.getLocation());
    }

    @Get("/dropped")
    public List<DroppedItem> getDroppedItems(@QueryValue String map, @QueryValue Integer x, @QueryValue Integer y) {
        Location location = new Location(map, x, y, null);
        return itemService.getItemsInMap(location);
    }

    @Post("/clear-data")
    public void clearAll() {
        // this is a test endpoint for clearing all DB data relating to items
        itemService.clearAllItemData();
    }
}
