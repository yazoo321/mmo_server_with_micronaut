package server.items.service;

import com.mongodb.client.result.DeleteResult;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.types.StatsTypes;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.items.model.DroppedItem;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.repository.ItemRepository;

@Slf4j
@Singleton
public class ItemService {

    @Inject ItemRepository itemRepository;

    protected Random rand = new Random();

    public Single<DroppedItem> createNewDroppedItem(String itemId, Location location) {
        return itemRepository
                .findByItemId(itemId)
                .doOnError(e -> log.error(e.getMessage()))
                .flatMap(foundItem -> createDroppedItem(foundItem, location));
    }

    private Single<DroppedItem> createDroppedItem(Item foundItem, Location location) {
        log.info("Creating dropped item: {}, at location: {}", foundItem, location);
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

    public Single<List<Item>> getItemsForLevels(int minLevel, int maxLevel) {
        return itemRepository.findItemsForLevels(minLevel, maxLevel);
    }

    public void handleItemDropsForMob(Stats mobStats, Motion motion) {
        log.info("Handling item drops for mob");
        if (mobStats.getBaseStats() == null) {
            log.error("mob stats were null on handle item drops");
            return;
        }

        int mobLevel = mobStats.getBaseStat(StatsTypes.LEVEL);
        if (mobLevel == 0) {
            return;
        }

        int minLevelItem = Math.max(1, mobLevel - 10);
        int maxLevelItem = mobLevel + 5;

        List<Item> availableItemsToDrop = getItemsForLevels(minLevelItem, maxLevelItem).blockingGet();

        availableItemsToDrop.forEach(i -> i.setDropChance(rand.nextInt(1000)));

        PriorityQueue<Item> itemQueue = new PriorityQueue<>((a, b) -> Integer.compare(b.getDropChance(), a.getDropChance()));

        itemQueue.addAll(availableItemsToDrop);

        List<Item> toDrop = handleDrop(1, itemQueue, new ArrayList<>());


        toDrop.forEach(item -> {
            Location location = new Location(motion);
            location.setX(location.getX() + rand.nextInt(-30, 30));
            location.setY(location.getY() + rand.nextInt(-30, 30));
            createDroppedItem(item, new Location(motion)).subscribe();
        });
    }

    private List<Item> handleDrop(int iteration, PriorityQueue<Item> itemQueue, List<Item> toDrop) {
        int chance = rand.nextInt(100);

        int reqChance = 100 / (iteration + 1);

        if (chance > reqChance) {
            Item item = selectItemToDrop(itemQueue);
            if (item == null) {
                return toDrop;
            }
            toDrop.add(item);
            return handleDrop(iteration + 1, itemQueue, toDrop);
        }

        return toDrop;
    }
    private Item selectItemToDrop(PriorityQueue<Item> itemQueue) {
        if (itemQueue.isEmpty()) {
            return null;
        }

        Item item = itemQueue.poll();
        int quality = item.getQuality() == null ? 1 : item.getQuality();

        int dropRequirement = switch (quality) {
            case 1 -> 500;
            case 2 -> 950;
            case 3 -> 990;
            case 4 -> 995;
            default -> 999;
        };

        if (item.getDropChance() >= dropRequirement) {
            log.info("Selected item: {}, Quality: {}, DropChance: {}", item.getItemName(), item.getQuality(), item.getDropChance());
            return item;
        } else {
            return selectItemToDrop(itemQueue);
        }
    }
}
