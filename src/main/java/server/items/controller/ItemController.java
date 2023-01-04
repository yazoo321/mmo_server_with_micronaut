package server.items.controller;

import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import java.util.List;
import server.common.dto.Location;
import server.items.dropped.model.DroppedItem;
import server.items.dropped.model.DroppedItemResponse;
import server.items.model.Item;
import server.items.service.ItemService;
import server.player.character.inventory.model.response.GenericInventoryData;

@Controller("/v1/items")
public class ItemController {

    @Inject ItemService itemService;

    @Post("/create-items")
    public List<Item> createItems(@Body GenericInventoryData data) {
        // This is a test endpoint!
        // you should not be creating items live, they should be handled via migration

        return itemService.createItems(data.getItems());
    }

    @Post("/spawn-item")
    public DroppedItem spawnItem(@Body GenericInventoryData inventoryData) {
        // TODO: this will be changed to a loot service. e.g. when mob is killed, spawn random items
        return itemService.createNewDroppedItem(
                inventoryData.getItemId(), inventoryData.getLocation());
    }

    @Get("/dropped")
    public DroppedItemResponse getDroppedItems(
            @QueryValue String map, @QueryValue Integer x, @QueryValue Integer y) {
        Location location = new Location(map, x, y, null);

        return new DroppedItemResponse(itemService.getItemsInMap(location));
    }
}
