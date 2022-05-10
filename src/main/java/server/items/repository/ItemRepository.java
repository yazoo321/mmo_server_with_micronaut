package server.items.repository;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Single;
import server.common.dto.Location;
import server.common.mongo.query.MongoDbQueryHelper;
import server.configuration.PlayerCharacterConfiguration;
import server.items.dropped.model.DroppedItem;
import server.items.dto.Item;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

@Singleton
public class ItemRepository {

    private final static Integer DROPPED_ITEM_TIMEOUT_SECONDS = 60;

    PlayerCharacterConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Item> itemCollection;
    MongoCollection<DroppedItem> droppedItemCollection;

    public ItemRepository(
            PlayerCharacterConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public List<DroppedItem> getItemsNear(Location location) {
        // filter by map + location of X, Y, Z co-ordinates
        return MongoDbQueryHelper.betweenLocation(droppedItemCollection, location, 100);
    }

    public DroppedItem createDroppedItem(DroppedItem droppedItem) {
        return Single.fromPublisher(
                droppedItemCollection.insertOne(droppedItem))
                .map(success -> droppedItem).blockingGet();
    }

    public DroppedItem findDroppedItemById(String droppedItemId) {
        return Single.fromPublisher(
                droppedItemCollection.find(
                        eq("droppedItemId", droppedItemId)
                )
        ).blockingGet();
    }

    public Item createItem(Item item) {
        return Single.fromPublisher(
                itemCollection.insertOne(item))
                .map(success -> item).blockingGet();
    }

    public Item findByItemId(String itemId) {
        return Single.fromPublisher(
                itemCollection
                        .find(eq("itemId", itemId))
        ).blockingGet();
    }

    public void deleteDroppedItem(String droppedItemId) {
        Single.fromPublisher(
                droppedItemCollection.deleteOne(eq("droppedItemId", droppedItemId)))
                .blockingGet();
    }

    public void deleteTimedOutDroppedItems() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(DROPPED_ITEM_TIMEOUT_SECONDS);
        Single.fromPublisher(
                droppedItemCollection.deleteMany(gt("droppedAt", cutoffTime)))
                .blockingGet();
    }

    public void deleteAllItemData() {
        // this is for test purposes
        Single.fromPublisher(
                itemCollection.deleteMany(ne("category", "deleteAll"))
        ).blockingGet();

        Single.fromPublisher(
                droppedItemCollection.deleteMany(ne("map", "deleteAll"))
        ).blockingGet();
    }

    private void prepareCollections() {
        this.itemCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), Item.class);

        this.droppedItemCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), DroppedItem.class);
    }
}
