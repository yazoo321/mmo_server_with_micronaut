package server.items.controller;

import io.micronaut.http.annotation.*;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.items.model.DroppedItem;
import server.items.model.DroppedItemResponse;
import server.items.model.Item;
import server.items.service.ItemService;
import server.items.inventory.model.response.GenericInventoryData;

@Slf4j
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
    public Single<DroppedItem> spawnItem(@Body GenericInventoryData inventoryData) {
        // TODO: this will be changed to a loot service. e.g. when mob is killed, spawn random items
        return itemService.createNewDroppedItem(
                inventoryData.getItemId(), inventoryData.getLocation());
    }

    @Get("/dropped")
    public Single<DroppedItemResponse> getDroppedItems(
            @QueryValue String map, @QueryValue Integer x, @QueryValue Integer y) {
        Location location = new Location(map, x, y, null);

        return itemService.getItemsInMap(location)
                .doOnError(e -> log.error("Failed to get items in map, {}", e.getMessage()))
                .map(DroppedItemResponse::new);
    }
}
