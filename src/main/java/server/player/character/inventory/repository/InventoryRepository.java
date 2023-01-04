package server.player.character.inventory.repository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.util.List;
import server.configuration.MongoConfiguration;
import server.player.character.inventory.model.CharacterItem;
import server.player.character.inventory.model.Inventory;
import server.player.character.inventory.model.exceptions.InventoryException;

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

    public Inventory getCharacterInventory(String characterName) {
        return Flowable.fromPublisher(
                        inventoryCollection.find(eq("characterName", characterName)).limit(1))
                .firstElement()
                .blockingGet();
    }

    public void updateInventoryItems(String characterName, List<CharacterItem> items) {
        UpdateResult res =
                Flowable.fromPublisher(
                                inventoryCollection.updateOne(
                                        eq("characterName", characterName),
                                        set("characterItems", items)))
                        .firstElement()
                        .blockingGet();

        if (res.getModifiedCount() < 1) {
            throw new InventoryException("Failed up update inventory items");
        }
    }

    public void updateInventoryMaxSize(Inventory inventory) {
        UpdateResult res =
                Single.fromPublisher(
                                inventoryCollection.updateOne(
                                        eq("characterName", inventory.getCharacterName()),
                                        set("maxSize", inventory.getMaxSize())))
                        .blockingGet();

        if (res.getModifiedCount() < 1) {
            throw new InventoryException("Failed up update inventory max size");
        }
    }

    public Inventory insert(Inventory inventory) {
        InsertOneResult res =
                Single.fromPublisher(inventoryCollection.insertOne(inventory)).blockingGet();

        return res.wasAcknowledged() ? inventory : null;
    }

    public void deleteAllInventoryDataForCharacter(String characterName) {
        // this is a test endpoint
        Single.fromPublisher(
                        inventoryCollection.findOneAndDelete(eq("characterName", characterName)))
                .blockingGet();
    }

    private void prepareCollections() {
        this.inventoryCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getInventoryCollection(), Inventory.class);
    }
}
