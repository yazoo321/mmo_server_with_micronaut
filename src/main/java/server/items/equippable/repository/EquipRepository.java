package server.items.equippable.repository;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import server.configuration.MongoConfiguration;
import server.items.equippable.model.EquippedItems;

import static com.mongodb.client.model.Filters.*;

@Slf4j
@Singleton
public class EquipRepository {

    // This repository is connected to MongoDB
    MongoConfiguration playerCharacterConfiguration;
    MongoClient mongoClient;
    MongoCollection<EquippedItems> equippedItemsCollection;

    public EquipRepository(
            MongoConfiguration playerCharacterConfiguration, MongoClient mongoClient) {
        this.playerCharacterConfiguration = playerCharacterConfiguration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public Single<EquippedItems> insert(EquippedItems equippedItems) {
        return Single.fromPublisher(equippedItemsCollection.insertOne(equippedItems))
                .map(res -> equippedItems);
    }

    public Single<List<EquippedItems>> getEquippedItemsForCharacter(String characterName) {
        return Flowable.fromPublisher(
                        equippedItemsCollection.find(eq("characterName", characterName)))
                .toList();
    }

    public Single<List<EquippedItems>> getEquippedItemsForCharacters(Set<String> characterNames) {
        return Flowable.fromPublisher(
                        equippedItemsCollection.find(in("characterName", characterNames)))
                .toList();
    }

    public Maybe<EquippedItems> getCharacterItemSlot(String characterName, String slotType) {
        return Flowable.fromPublisher(
                        equippedItemsCollection.find(
                                and(eq("characterName", characterName), eq("category", slotType))))
                .firstElement();
    }

    public Single<DeleteResult> deleteEquippedItem(String itemInstanceId) {
        // TODO: Consider duplicating item instance ID as nested query is slower
        return Single.fromPublisher(
                equippedItemsCollection.deleteOne(
                        eq("itemInstance.itemInstanceId", itemInstanceId)));
    }

    private void prepareCollections() {
        this.equippedItemsCollection =
                mongoClient
                        .getDatabase(playerCharacterConfiguration.getDatabaseName())
                        .getCollection(
                                playerCharacterConfiguration.getEquipCollection(),
                                EquippedItems.class);
    }
}
