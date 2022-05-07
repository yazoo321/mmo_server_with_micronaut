package server.player.character.inventory.repository;

import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Flowable;
import server.configuration.PlayerCharacterConfiguration;
import server.player.character.inventory.model.CharacterItem;
import server.player.character.inventory.model.Inventory;
import server.player.character.inventory.model.exceptions.InventoryException;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

@Singleton
public class InventoryRepository {


    // This repository is connected to MongoDB
    PlayerCharacterConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Inventory> inventoryCollection;

    public InventoryRepository(
            PlayerCharacterConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public Inventory getCharacterInventory(String characterName) {
        return Flowable.fromPublisher(
                inventoryCollection
                        .find(eq("characterName", characterName))
                        .limit(1)
        ).firstElement().blockingGet();
    }

    public void updateInventoryItems(String characterName, List<CharacterItem> items) {
        UpdateResult res = Flowable.fromPublisher(
                inventoryCollection
                        .updateOne(eq("characterName", characterName), set("characterItems", items)))
                .firstElement()
                .blockingGet();

        if (res.getModifiedCount() < 1) {
            throw new InventoryException("Failed up update inventory items");
        }
    }

    public Inventory removeItemFromInventory(Inventory inventory, CharacterItem item) throws InventoryException {
        List<CharacterItem> items = inventory.getCharacterItems();
        List<CharacterItem> found = items.stream().filter(i -> i.getLocation().matches(item.getLocation()))
                .collect(Collectors.toList());
        if (found.size() > 0) {
            items.remove(found.get(0));
            UpdateResult res = Flowable.fromPublisher(
                    inventoryCollection.updateOne(eq("characterName", inventory.getCharacterName()),
                    set("characterItems", items)))
            .firstElement()
            .blockingGet();

            if (res.getModifiedCount() < 1) {
                throw new InventoryException("Failed to update the inventory when removing item");
            }
        } else {
            throw new InventoryException("Did not find item to remove from inventory");
        }

        return inventory;
    }

    private void prepareCollections() {
        this.inventoryCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), Inventory.class);
    }
}
