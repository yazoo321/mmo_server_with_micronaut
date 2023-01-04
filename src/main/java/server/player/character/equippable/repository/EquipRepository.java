package server.player.character.equippable.repository;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import server.configuration.MongoConfiguration;
import server.player.character.equippable.model.EquippedItems;
import server.player.character.equippable.model.exceptions.EquipException;

import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Slf4j
@Singleton
public class EquipRepository {

    // This repository is connected to MongoDB
    MongoConfiguration playerCharacterConfiguration;
    MongoClient mongoClient;
    MongoCollection<EquippedItems> equippedItemsCollection;

    public EquipRepository(
            MongoConfiguration playerCharacterConfiguration,
            MongoClient mongoClient) {
        this.playerCharacterConfiguration = playerCharacterConfiguration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }


    public EquippedItems insert(EquippedItems equippedItems) {
        InsertOneResult res = Single.fromPublisher(
                equippedItemsCollection.insertOne(equippedItems)
        ).blockingGet();

        return res.wasAcknowledged() ? equippedItems : null;
    }

    public List<EquippedItems> getEquippedItemsForCharacter(String characterName) {
        try {
                return Flowable.fromPublisher(
                    equippedItemsCollection.find(
                            eq("characterName", characterName)
                    )
            ).toList().blockingGet();
        } catch (NoSuchElementException e) {
            // this is fine
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to get character items, {}", e.getMessage());
            throw new EquipException("Failed to get equipped items");
        }
    }

    public EquippedItems getCharacterItemSlot(String characterName, String slotType) {
        try {
            return Single.fromPublisher(
                    equippedItemsCollection.find(
                            and(
                                    eq("characterName", characterName),
                                    eq("category", slotType)
                            )
                    )
            ).blockingGet();
        } catch(NoSuchElementException e) {
            // this is standard expected state
            return null;
        } catch (Exception e) {
            // this is unexpected, either too many rows or something else. log the error
            log.error("Failed to get equipped items. {}", e.getMessage());
            // abort action
            throw e;
        }
    }

    public boolean deleteEquippedItem(String itemInstanceId) {
        // TODO: Consider duplicating item instance ID as nested query is slower
        DeleteResult res = Single.fromPublisher(
                equippedItemsCollection.deleteOne(
                        eq("itemInstance.itemInstanceId", itemInstanceId)
                )
        ).blockingGet();

        return res.getDeletedCount() > 0;
    }
    private void prepareCollections() {
        this.equippedItemsCollection = mongoClient
                .getDatabase(playerCharacterConfiguration.getDatabaseName())
                .getCollection(playerCharacterConfiguration.getEquipCollection(), EquippedItems.class);
    }
}
