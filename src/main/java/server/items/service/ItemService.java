package server.items.service;

import com.mongodb.client.result.DeleteResult;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.items.model.DroppedItem;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.repository.ItemRepository;
import server.items.server_integration.producer.ItemServerProducer;

@Slf4j
@Singleton
public class ItemService {

    @Inject ItemRepository itemRepository;

    ItemServerProducer itemServerProducer;

    public ItemService(@KafkaClient("item-client") ItemServerProducer itemServerProducer) {
        this.itemServerProducer = itemServerProducer;
    }

    public Single<DroppedItem> createNewDroppedItem(String itemId, Location location) {
        LocalDateTime now = LocalDateTime.now();

        return itemRepository
                .findByItemId(itemId)
                .doOnError(
                        e ->
                                log.error(
                                        "Failed to find item when creating dropped item, {}",
                                        e.getMessage()))
                .flatMap(
                        foundItem -> {
                            String itemInstanceId = UUID.randomUUID().toString();
                            String droppedItemId =
                                    UUID.randomUUID()
                                            .toString(); // generate unique ID for the dropped item

                            ItemInstance instance =
                                    new ItemInstance(itemId, itemInstanceId, foundItem);
                            return itemRepository
                                    .createItemInstance(instance)
                                    .doOnError(
                                            e ->
                                                    log.error(
                                                            "failed to generate item instance for"
                                                                    + " dropped item, {}",
                                                            e.getMessage()))
                                    .flatMap(
                                            ins -> {
                                                DroppedItem droppedItem =
                                                        new DroppedItem(
                                                                droppedItemId,
                                                                location,
                                                                instance,
                                                                now);
                                                return itemRepository.createDroppedItem(
                                                        droppedItem);
                                            });
                        });
    }

    public Single<DroppedItem> dropExistingItem(String itemInstanceId, Location location) {
        LocalDateTime now = LocalDateTime.now();
        String uuid = UUID.randomUUID().toString(); // generate unique ID for the dropped item
        return itemRepository
                .findItemInstanceById(itemInstanceId)
                .doOnError(
                        e ->
                                log.error(
                                        "Failed to get item instance to drop item, {}",
                                        e.getMessage()))
                .flatMap(
                        itemInstance -> {
                            DroppedItem droppedItem =
                                    new DroppedItem(uuid, location, itemInstance, now);
                            return itemRepository
                                    .createDroppedItem(droppedItem)
                                    .doOnError(
                                            e ->
                                                    log.error(
                                                            "failed to create dropped item, {}",
                                                            e.getMessage()))
                                    .map(item -> item);
                        });
    }

    public Single<DroppedItem> getDroppedItemById(String droppedItemId) {
        return itemRepository.findDroppedItemById(droppedItemId);
    }

    public Single<List<DroppedItem>> getItemsInMap(Location location) {
        return itemRepository.getItemsNear(location);
    }

    public Single<DeleteResult> deleteDroppedItem(String droppedItemId) {
        return itemRepository.deleteDroppedItem(droppedItemId);
    }

    public List<Item> createItems(List<Item> items) {
        List<Item> created = new ArrayList<>();
        // this is almost never used so can be blocking
        for (Item i : items) {
            created.add(itemRepository.createItem(i).blockingGet());
        }

        return created;
    }
}
