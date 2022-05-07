package server.items.controller;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import server.items.dropped.model.DroppedItem;
import server.items.dto.Item;
import server.items.service.ItemService;

import javax.inject.Inject;
import java.security.Principal;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/v1/items")
public class ItemController {

    @Inject
    ItemService itemService;

    @Post("/create-item")
    public Item updatePlayerLocation(Principal principal, @Body Item itemToCreate) {
        // This is a test endpoint!
        // you should not be creating items live, they should be handled via migration

        return itemService.createItem(itemToCreate);
    }

    @Post("/spawn-item")
    public DroppedItem spawnItem(Principal principal, @Body DroppedItem droppedItem) {
        // TODO: this will be changed to a loot service. e.g. when mob is killed, spawn random items
        return itemService.dropItem(droppedItem.getItem(), droppedItem.getLocation());
    }

}
