package server.items.repository;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.mongo.query.MongoDbQueryHelper;
import server.configuration.MongoConfiguration;
import server.items.model.DroppedItem;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.model.exceptions.ItemException;

@Slf4j
@Singleton
public class ItemRepository {

    private static final Integer DROPPED_ITEM_TIMEOUT_SECONDS = 60;

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Item> itemCollection;
    MongoCollection<DroppedItem> droppedItemCollection;
    MongoCollection<ItemInstance> itemInstanceCollection;

    public ItemRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public List<DroppedItem> getItemsNear(Location location) {
        // filter by map + location of X, Y, Z co-ordinates
        return MongoDbQueryHelper.betweenLocation(droppedItemCollection, location, 1000);
    }

    public Single<List<DroppedItem>> getItemsNearAsync(Location location) {
        return MongoDbQueryHelper.betweenLocationAsync(droppedItemCollection, location, 1000);
    }

    public DroppedItem createDroppedItem(DroppedItem droppedItem) {
        return Single.fromPublisher(droppedItemCollection.insertOne(droppedItem))
                .map(success -> droppedItem)
                .blockingGet();
    }

    public DroppedItem findDroppedItemById(String droppedItemId) {
        try {
            return Single.fromPublisher(
                            droppedItemCollection.find(eq("droppedItemId", droppedItemId)))
                    .blockingGet();
        } catch (NoSuchElementException e) {
            log.warn("Could not find the dropped item by ID!");
            throw new ItemException("Could not find the dropped item by ID!");
        }
    }

    public Item createItem(Item item) {
        try {
            return findByItemId(item.getItemId());
            // if item is found, we want to ignore creating this item and just return the found
            // item.
        } catch (ItemException e) {
            return Single.fromPublisher(itemCollection.insertOne(item))
                    .map(success -> item)
                    .blockingGet();
        }
    }

    public Item findByItemId(String itemId) {
        try {
            return Single.fromPublisher(itemCollection.find(eq("itemId", itemId))).blockingGet();
        } catch (NoSuchElementException e) {
            log.error("Could not find the item by ID! It no longer exists");
            throw new ItemException("Could not find the item by ID! It no longer exists");
        }
    }

    public ItemInstance createItemInstance(ItemInstance itemInstance) {
        try {
            return Single.fromPublisher(itemInstanceCollection.insertOne(itemInstance))
                    .map(success -> itemInstance)
                    .blockingGet();
        } catch (Exception e) {
            log.error("Failed to create item instance, {}", e.getMessage());

            throw new ItemException("Failed to create item instance");
        }
    }

    public ItemInstance findItemInstanceById(String instanceId) {
        try {
            return Single.fromPublisher(
                            itemInstanceCollection.find(eq("itemInstanceId", instanceId)))
                    .blockingGet();
        } catch (NoSuchElementException e) {
            log.error("Could not find the item instance by ID! It no longer exists");
            throw new ItemException("Could not find the item instance by ID! It no longer exists");
        }
    }

    public List<ItemInstance> findItemByItemInstanceIds(List<String> itemInstanceIds) {
        if (null == itemInstanceIds || itemInstanceIds.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return Flowable.fromPublisher(
                            itemInstanceCollection.find(in("itemInstanceId", itemInstanceIds)))
                    .toList()
                    .blockingGet();
        } catch (NoSuchElementException e) {
            log.error("Could not find the instances for given instance IDs, {}", itemInstanceIds);
            throw new ItemException("Could not find the instances for given instance IDs");
        }
    }

    public void deleteDroppedItem(String droppedItemId) {
        try {
            Single.fromPublisher(
                            droppedItemCollection.deleteOne(eq("droppedItemId", droppedItemId)))
                    .blockingGet();
        } catch (NoSuchElementException e) {
            // this could be race condition
            log.warn("Deleting dropped item failed as item was not found!");
            throw new ItemException("Deleting dropped item failed as item was not found!");
        }
    }

    public void deleteTimedOutDroppedItems() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(DROPPED_ITEM_TIMEOUT_SECONDS);
        Single.fromPublisher(droppedItemCollection.deleteMany(gt("droppedAt", cutoffTime)))
                .blockingGet();
    }

    private void prepareCollections() {
        this.itemCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getItemsCollection(), Item.class);

        this.droppedItemCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(
                                configuration.getDroppedItemsCollection(), DroppedItem.class);

        this.itemInstanceCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(
                                configuration.getItemInstancesCollection(), ItemInstance.class);
    }
}
