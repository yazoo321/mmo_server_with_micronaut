package server.items.repository;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Flowable;
import io.reactivex.Single;
import server.configuration.PlayerCharacterConfiguration;
import server.items.dropped.model.DroppedItem;
import server.items.dto.Item;

import javax.inject.Singleton;
import java.time.LocalDateTime;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;

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

    public DroppedItem createDroppedItem(DroppedItem droppedItem) {
        return Single.fromPublisher(
                droppedItemCollection.insertOne(droppedItem))
                .map(success -> droppedItem).blockingGet();
    }

    public Item createItem(Item item) {
        return Single.fromPublisher(
                itemCollection.insertOne(item))
                .map(success -> item).blockingGet();
    }

    public Item findByItemId(String itemId) {
        return Flowable.fromPublisher(
                itemCollection
                        .find(eq("itemId", itemId))
                        .limit(1)
        ).firstElement().blockingGet();
    }

    public void deleteDroppedItem(DroppedItem item) {
        Single.fromPublisher(
                droppedItemCollection.deleteOne(eq("itemId", item.getItem().getItemId())))
                .blockingGet();
    }

    public void deleteTimedOutDroppedItems() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(DROPPED_ITEM_TIMEOUT_SECONDS);
        Single.fromPublisher(
                droppedItemCollection.deleteMany(gt("droppedAt", cutoffTime)))
                .blockingGet();
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
