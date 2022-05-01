package server.player.character.inventory.repository;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Flowable;
import server.configuration.PlayerCharacterConfiguration;
import server.player.character.inventory.model.Inventory;

import javax.inject.Singleton;

import static com.mongodb.client.model.Filters.eq;

@Singleton
public class PlayerItemsRepository {


    // This repository is connected to MongoDB
    PlayerCharacterConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Inventory> inventory;

    public PlayerItemsRepository(
            PlayerCharacterConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public Inventory getUserInventory(String characterName) {
        return Flowable.fromPublisher(
                inventory
                        .find(eq("characterName", characterName))
                        .limit(1)
        ).firstElement().blockingGet();
    }

    private void prepareCollections() {
        this.inventory = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), Inventory.class);
    }
}
