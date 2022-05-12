package server.items.helper;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Single;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.common.dto.Tag;
import server.configuration.PlayerCharacterConfiguration;
import server.items.dropped.model.DroppedItem;
import server.items.model.Item;
import server.items.model.Stacking;
import server.items.weapons.Weapon;
import server.player.character.inventory.model.Inventory;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.ne;

@Singleton
public class ItemTestHelper {

    PlayerCharacterConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Item> itemCollection;
    MongoCollection<DroppedItem> droppedItemCollection;
    MongoCollection<Inventory> inventoryMongoCollection;

    public ItemTestHelper(
            PlayerCharacterConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public void deleteAllItemData() {
        // this is for test purposes
        Single.fromPublisher(
                itemCollection.deleteMany(ne("category", "deleteAll"))
        ).blockingGet();

        Single.fromPublisher(
                droppedItemCollection.deleteMany(ne("map", "deleteAll"))
        ).blockingGet();

        Single.fromPublisher(
                inventoryMongoCollection.deleteMany(ne("characterName", "deleteAll"))
        ).blockingGet();
    }

    public static Weapon createTestWeaponItem() {
        List<Tag> weaponTags = List.of(
                new Tag("damage", "30")
        );
        Stacking stacOpts = new Stacking(false, 1, 1);

        return new Weapon("123", "weapon name", weaponTags, stacOpts, 1000);
    }

    public Weapon createAndInsertWeaponItem() {
        Weapon weapon = createTestWeaponItem();

        return Single.fromPublisher(
                itemCollection.insertOne(weapon)
        ).map(success -> weapon).blockingGet();
    }

    public static DroppedItem createDroppedItem(Location location, Item item) {
        return new DroppedItem("random-uuid", location, item, LocalDateTime.now());
    }

    public DroppedItem createAndInsertDroppedItem(Location location, Item item) {
        DroppedItem droppedItem = createDroppedItem(location, item);

        return Single.fromPublisher(
                droppedItemCollection.insertOne(droppedItem)
        ).map(success -> droppedItem).blockingGet();
    }

    public Inventory prepareInventory(String characterName) {
        Inventory inventory = new Inventory();

        inventory.setCharacterName(characterName);
        inventory.setCharacterItems(new ArrayList<>());
        inventory.setGold(0);
        inventory.setMaxSize(new Location2D(10, 10));

        return insertInventory(inventory);
    }

    public Inventory insertInventory(Inventory inventory) {
        return Single.fromPublisher(
                inventoryMongoCollection.insertOne(inventory)
        ).map(success -> inventory).blockingGet();
    }

    private void prepareCollections() {
        this.itemCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), Item.class);

        this.droppedItemCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), DroppedItem.class);

        this.inventoryMongoCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), Inventory.class);
    }
}
