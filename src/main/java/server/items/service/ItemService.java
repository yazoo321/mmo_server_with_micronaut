package server.items.service;

import com.mongodb.client.result.DeleteResult;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.items.model.DroppedItem;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.repository.ItemRepository;

@Slf4j
@Singleton
public class ItemService {

    @Inject ItemRepository itemRepository;

    public Single<DroppedItem> createNewDroppedItem(String itemId, Location location) {
        return itemRepository
                .findByItemId(itemId)
                .doOnError(e -> log.error(e.getMessage()))
                .flatMap(foundItem -> createDroppedItem(foundItem, location));
    }

    private Single<DroppedItem> createDroppedItem(Item foundItem, Location location) {
        String itemInstanceId = UUID.randomUUID().toString();

        ItemInstance instance = new ItemInstance(foundItem.getItemId(), itemInstanceId, foundItem);
        return itemRepository
                .upsertItemInstance(instance)
                .doOnError(e -> log.error(e.getMessage()))
                .flatMap(ins -> createDroppedItem(location, ins));
    }

    private Single<DroppedItem> createDroppedItem(Location location, ItemInstance itemInstance) {
        DroppedItem droppedItem =
                new DroppedItem(
                        itemInstance.getItemInstanceId(), location, itemInstance, Instant.now());
        return itemRepository.createDroppedItem(droppedItem);
    }

    public Single<DroppedItem> dropExistingItem(String itemInstanceId, Location location) {
        return itemRepository
                .findItemInstanceById(itemInstanceId)
                .doOnError(e -> log.error(e.getMessage()))
                .flatMap(itemInstance -> createDroppedItem(location, itemInstance));
    }

    public Single<DroppedItem> getDroppedItemByInstanceId(String instanceId) {
        return itemRepository.findDroppedItemByInstanceId(instanceId);
    }

    public Single<List<DroppedItem>> getItemsInMap(Location location) {
        return itemRepository.getItemsNear(location);
    }

    public Single<DeleteResult> deleteDroppedItem(String itemInstanceId) {
        return itemRepository.deleteDroppedItem(itemInstanceId);
    }

    public List<Item> createItems(List<Item> items) {
        List<Item> created = new ArrayList<>();
        // this is almost never used so can be blocking
        for (Item i : items) {
            created.add(itemRepository.upsertItem(i).blockingGet());
        }

        return created;
    }
}
