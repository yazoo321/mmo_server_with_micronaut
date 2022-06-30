package server.items.service;

import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.items.dropped.model.DroppedItem;
import server.items.dropped.model.DroppedItemDto;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.model.exceptions.ItemException;
import server.items.repository.ItemRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Singleton
public class ItemService {

    @Inject
    ItemRepository itemRepository;

    public DroppedItemDto createNewDroppedItem(String itemId, Location location) {
        LocalDateTime now = LocalDateTime.now();

        Item foundItem = itemRepository.findByItemId(itemId);
        if (foundItem == null) {
            log.error("Failed to create new dropped item - item id not recognised");
            throw new ItemException("Failed to create new dropped item - check Item ID");
        }
        String itemInstanceId = UUID.randomUUID().toString();
        String droppedItemId = UUID.randomUUID().toString(); // generate unique ID for the dropped item

        ItemInstance instance = new ItemInstance(itemId, itemInstanceId, new ArrayList<>());
        instance = itemRepository.createItemInstance(instance);
        DroppedItem droppedItem = new DroppedItem(droppedItemId, location, instance.getItemInstanceId(), now);
        droppedItem = itemRepository.createDroppedItem(droppedItem);

        return new DroppedItemDto(droppedItem, foundItem, instance);
    }

    public DroppedItemDto dropExistingItem(String itemInstanceId, Location location) {
        LocalDateTime now = LocalDateTime.now();
        String uuid = UUID.randomUUID().toString(); // generate unique ID for the dropped item
        DroppedItem droppedItem = new DroppedItem(uuid, location, itemInstanceId, now);

        ItemInstance instance = itemRepository.findItemInstanceById(itemInstanceId);
        Item item = itemRepository.findByItemId(instance.getItemId());
        droppedItem = itemRepository.createDroppedItem(droppedItem);

        return new DroppedItemDto(droppedItem, item, instance) ;
    }

    public DroppedItem getDroppedItemById(String droppedItemId) {
        return itemRepository.findDroppedItemById(droppedItemId);
    }

    public List<DroppedItemDto> getItemsInMap(Location location) {
        List<DroppedItem> droppedItems = itemRepository.getItemsNear(location);
        List<DroppedItemDto> droppedItemDtos = new ArrayList<>();

        // TODO: performance improvement here, pre-load the data
        droppedItems.forEach(i -> {
            ItemInstance instance = itemRepository.findItemInstanceById(i.getItemInstanceId());
            Item item = itemRepository.findByItemId(instance.getItemId());
            droppedItemDtos.add(
                    new DroppedItemDto(i, item, instance)
            );
        });

        return droppedItemDtos;
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
