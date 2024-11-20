package server.items.repository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.Cacheable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import server.attribute.stats.types.StatsTypes;
import server.common.configuration.MongoConfiguration;
import server.common.dto.Location;
import server.common.mongo.query.MongoDbQueryHelper;
import server.items.model.DroppedItem;
import server.items.model.Item;
import server.items.model.ItemInstance;

@Slf4j
@Singleton
@CacheConfig("itemCache")
public class ItemRepository {

    private static final String ITEM_CACHE = "item-cache";

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
        Bson filter = Filters.eq("itemInstanceId", droppedItem.getItemInstanceId());
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        return Single.fromPublisher(droppedItemCollection.replaceOne(filter, droppedItem, options))
                .map(res -> droppedItem);
    }

    public Single<DroppedItem> findDroppedItemByInstanceId(String itemInstanceId) {
        return Single.fromPublisher(
                droppedItemCollection.find(eq("itemInstanceId", itemInstanceId)));
    }

    public Single<Item> upsertItem(Item item) {
        Bson filter = Filters.eq("itemId", item.getItemId());
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        return Single.fromPublisher(itemCollection.replaceOne(filter, item, options))
                .map(res -> item);
    }

    public Single<Item> findByItemId(String itemId) {
        return Single.fromPublisher(itemCollection.find(eq("itemId", itemId)));
    }

    public Single<ItemInstance> upsertItemInstance(ItemInstance itemInstance) {
        Bson filter = Filters.eq("itemInstanceId", itemInstance.getItemInstanceId());
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        return Single.fromPublisher(
                        itemInstanceCollection.replaceOne(filter, itemInstance, options))
                .map(res -> itemInstance);
    }

    public Single<ItemInstance> findItemInstanceById(String instanceId) {
        return Single.fromPublisher(itemInstanceCollection.find(eq("itemInstanceId", instanceId)));
    }

    public Single<DeleteResult> deleteDroppedItem(String itemInstanceId) {
        return Single.fromPublisher(
                droppedItemCollection.deleteOne(eq("itemInstanceId", itemInstanceId)));
    }


    @Cacheable(
            value = ITEM_CACHE,
            parameters = {"minLevel", "maxLevel"})
    public Single<List<Item>> findItemsForLevels(int minLevel, int maxLevel) {
        int baseStat = 15;
        int pointsPerLevel = 5;

        int minStatSum = (baseStat * 4) + pointsPerLevel * (minLevel - 1);
        int maxStatSum = (baseStat * 4) + pointsPerLevel * (maxLevel - 1);

        // Build aggregation pipeline
        List<Bson> pipeline = Arrays.asList(
                // Add a computed field for the sum of requirements
                Aggregates.addFields(
                        new Field<>("totalStatRequirements",
                                new Document("$add", Arrays.asList(
                                        "$requirements." + StatsTypes.STR.getType(),
                                        "$requirements." + StatsTypes.INT.getType(),
                                        "$requirements." + StatsTypes.DEX.getType(),
                                        "$requirements." + StatsTypes.STA.getType()
                                ))
                        )
                ),
                // Filter items by the computed field
                Aggregates.match(
                        Filters.and(
                                Filters.gte("totalStatRequirements", minStatSum),
                                Filters.lte("totalStatRequirements", maxStatSum)
                        )
                )
        );

        return Flowable.fromPublisher(itemCollection.aggregate(pipeline, Item.class)).toList();
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
