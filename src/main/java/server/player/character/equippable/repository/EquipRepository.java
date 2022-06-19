package server.player.character.equippable.repository;

import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Single;
import server.configuration.PlayerCharacterConfiguration;
import server.player.character.equippable.model.EquippedItems;

import javax.inject.Singleton;

import static com.mongodb.client.model.Filters.eq;

@Singleton
public class EquipRepository {

    // This repository is connected to MongoDB
    PlayerCharacterConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<EquippedItems> equippedItemsCollection;

    public EquipRepository(
            PlayerCharacterConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }


    public EquippedItems insert(EquippedItems equippedItems) {
        InsertOneResult res = Single.fromPublisher(
                equippedItemsCollection.insertOne(equippedItems)
        ).blockingGet();

        return res.wasAcknowledged() ? equippedItems : null;
    }

    public EquippedItems getEquippedItemsForCharacter(String characterName) {
        return Single.fromPublisher(
                equippedItemsCollection.find(
                        eq("characterName", characterName)
                )
        ).blockingGet();
    }


    private void prepareCollections() {
        this.equippedItemsCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), EquippedItems.class);
    }
}
