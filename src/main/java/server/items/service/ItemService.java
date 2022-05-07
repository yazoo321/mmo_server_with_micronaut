package server.items.service;

import server.common.dto.Location;
import server.items.dropped.model.DroppedItem;
import server.items.dto.Item;
import server.items.repository.ItemRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;

@Singleton
public class ItemService {

    // maybe consider adding loot tables etc here?

    @Inject
    ItemRepository itemRepository;


    public DroppedItem dropItem(Item item, Location location) {
        LocalDateTime now = LocalDateTime.now();
        DroppedItem droppedItem = new DroppedItem(item.getItemId(), location, item, now);

        droppedItem = itemRepository.createDroppedItem(droppedItem);

        return droppedItem;
    }

    public void deleteDroppedItem(DroppedItem item) {
        itemRepository.deleteDroppedItem(item);
    }

    public Item createItem(Item item) {
        return itemRepository.createItem(item);
    }
}
