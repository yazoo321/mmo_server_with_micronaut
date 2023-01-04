package server.items.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.items.dropped.model.DroppedItem;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.model.exceptions.ItemException;
import server.items.repository.ItemRepository;

@Slf4j
@Singleton
public class ItemService {

    @Inject ItemRepository itemRepository;

    public DroppedItem createNewDroppedItem(String itemId, Location location) {
        LocalDateTime now = LocalDateTime.now();

        Item foundItem = itemRepository.findByItemId(itemId);
        if (foundItem == null) {
            log.error("Failed to create new dropped item - item id not recognised");
            throw new ItemException("Failed to create new dropped item - check Item ID");
        }
        String itemInstanceId = UUID.randomUUID().toString();
        String droppedItemId =
                UUID.randomUUID().toString(); // generate unique ID for the dropped item

        ItemInstance instance = new ItemInstance(itemId, itemInstanceId, foundItem);
        instance = itemRepository.createItemInstance(instance);
        DroppedItem droppedItem = new DroppedItem(droppedItemId, location, instance, now);
        droppedItem = itemRepository.createDroppedItem(droppedItem);

        return droppedItem;
    }

    public DroppedItem dropExistingItem(String itemInstanceId, Location location) {
        LocalDateTime now = LocalDateTime.now();
        String uuid = UUID.randomUUID().toString(); // generate unique ID for the dropped item
        ItemInstance itemInstance = itemRepository.findItemInstanceById(itemInstanceId);
        DroppedItem droppedItem = new DroppedItem(uuid, location, itemInstance, now);

        return itemRepository.createDroppedItem(droppedItem);
    }

    public DroppedItem getDroppedItemById(String droppedItemId) {
        return itemRepository.findDroppedItemById(droppedItemId);
    }

    public List<DroppedItem> getItemsInMap(Location location) {
        return itemRepository.getItemsNear(location);
    }

    public void deleteDroppedItem(String droppedItemId) {
        itemRepository.deleteDroppedItem(droppedItemId);
    }

    public List<Item> createItems(List<Item> items) {
        List<Item> created = new ArrayList<>();
        for (Item i : items) {
            created.add(itemRepository.createItem(i));
        }

        return created;
    }
}
