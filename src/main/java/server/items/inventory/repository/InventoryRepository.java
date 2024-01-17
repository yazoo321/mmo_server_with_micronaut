package server.items.inventory.repository;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import org.bson.conversions.Bson;
import server.common.configuration.MongoConfiguration;
import server.items.inventory.model.CharacterItem;
import server.items.inventory.model.Inventory;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

@Singleton
public class InventoryRepository {

    // This repository is connected to MongoDB
    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Inventory> inventoryCollection;

    public InventoryRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public Single<Inventory> getCharacterInventory(String actorId) {
        return Single.fromPublisher(inventoryCollection.find(eq("actorId", actorId)));
    }

    public Single<UpdateResult> updateInventoryItems(String actorId, List<CharacterItem> items) {
        return Single.fromPublisher(
                inventoryCollection.updateOne(
                        eq("actorId", actorId), set("characterItems", items)));
    }

    public Single<UpdateResult> updateInventoryMaxSize(Inventory inventory) {
        return Single.fromPublisher(
                inventoryCollection.updateOne(
                        eq("actorId", inventory.getActorId()),
                        set("maxSize", inventory.getMaxSize())));
    }

    public Single<Inventory> upsert(Inventory inventory) {
        Bson filter = Filters.eq("actorId", inventory.getActorId());
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        return Single.fromPublisher(inventoryCollection.replaceOne(filter, inventory, options))
                .map(res -> inventory);
    }

    public Single<DeleteResult> deleteAllInventoryDataForCharacter(String actorId) {
        return Single.fromPublisher(inventoryCollection.deleteOne(eq("actorId", actorId)));
    }

    private void prepareCollections() {
        this.inventoryCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getInventoryCollection(), Inventory.class);
    }
}
