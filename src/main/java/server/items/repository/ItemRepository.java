package server.items.repository;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
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
import server.socket.producer.UpdateProducer;

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

    public Single<List<DroppedItem>> getItemsNear(Location location) {
        return MongoDbQueryHelper.betweenLocation(droppedItemCollection, location, 1000);
    }

    public Single<DroppedItem> createDroppedItem(DroppedItem droppedItem) {
        return Single.fromPublisher(droppedItemCollection.insertOne(droppedItem))
                .map(success -> droppedItem);
    }

    public Single<DroppedItem> findDroppedItemById(String droppedItemId) {
        return Single.fromPublisher(
                droppedItemCollection.find(eq("droppedItemId", droppedItemId)))
                .doOnError(e -> {
                    throw new ItemException("Failed to find dropped item by id");
                });
    }

    public Single<Item> createItem(Item item) {
        return Single.fromPublisher(itemCollection.replaceOne(
                eq("itemId", item.getItemId()),
                item,
                new ReplaceOptions().upsert(true)
        )).map(res -> item);
    }

    public Single<Item> findByItemId(String itemId) {
        return Single.fromPublisher(itemCollection.find(eq("itemId", itemId)));
    }

    public Single<ItemInstance> createItemInstance(ItemInstance itemInstance) {
        return Single.fromPublisher(itemInstanceCollection.insertOne(itemInstance))
                .map(success -> itemInstance);
    }

    public Single<ItemInstance> findItemInstanceById(String instanceId) {
        return Single.fromPublisher(
                itemInstanceCollection.find(eq("itemInstanceId", instanceId)));
    }

    public Single<List<ItemInstance>> findItemByItemInstanceIds(List<String> itemInstanceIds) {
        if (null == itemInstanceIds || itemInstanceIds.isEmpty()) {
            return Single.just(new ArrayList<>());
        }

        return Flowable.fromPublisher(
                        itemInstanceCollection.find(in("itemInstanceId", itemInstanceIds)))
                .toList();
    }

    public Single<DeleteResult> deleteDroppedItem(String droppedItemId) {
        return Single.fromPublisher(
                droppedItemCollection.deleteOne(eq("droppedItemId", droppedItemId)));
    }

    public Single<DeleteResult> deleteTimedOutDroppedItems() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(DROPPED_ITEM_TIMEOUT_SECONDS);
        return Single.fromPublisher(droppedItemCollection.deleteMany(gt("droppedAt", cutoffTime)));
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
