package server.items.repository;

import com.mongodb.client.result.UpdateResult;
import com.mongodb.internal.bulk.UpdateRequest;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.mongo.query.MongoDbQueryHelper;
import server.configuration.PlayerCharacterConfiguration;
import server.items.dropped.model.DroppedItem;
import server.items.model.Item;
import server.items.model.exceptions.ItemException;

import javax.inject.Singleton;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;

@Slf4j
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
        return MongoDbQueryHelper.betweenLocation(droppedItemCollection, location, 1000);
    }

    public DroppedItem createDroppedItem(DroppedItem droppedItem) {
        return Single.fromPublisher(
                droppedItemCollection.insertOne(droppedItem))
                .map(success -> droppedItem).blockingGet();
    }

    public DroppedItem findDroppedItemById(String droppedItemId) {
        try {
            return Single.fromPublisher(
                    droppedItemCollection.find(
                            eq("droppedItemId", droppedItemId)
                    )
            ).blockingGet();
        } catch (NoSuchElementException e) {
            log.warn("Could not find the dropped item by ID!");
            throw new ItemException("Could not find the dropped item by ID!");
        }
    }

    public Item createItem(Item item) {
        return Single.fromPublisher(
                itemCollection.insertOne(item))
                .map(success -> item).blockingGet();
    }

    public Item findByItemId(String itemId) {
        try {
            return Single.fromPublisher(
                    itemCollection
                            .find(eq("id", itemId))
            ).blockingGet();
        } catch (NoSuchElementException e) {
            log.error("Could not find the item by ID! It no longer exists");
            throw new ItemException("Could not find the item by ID! It no longer exists");
        }
    }

    public Boolean updateAppearanceId(String itemId, String appearanceId) {
        try {
            UpdateResult res = Single.fromPublisher(
                    itemCollection.updateOne(eq("id", itemId), set("appearancePieceId", appearanceId))
            ).blockingGet();

            return res.wasAcknowledged();
        } catch (Exception e) {
            log.error("Failed to update appearance ID on item!");
            throw new ItemException("Could not update the appearance ID on item!");
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
