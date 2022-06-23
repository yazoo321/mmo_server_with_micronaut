package server.items.helper;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Single;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.common.dto.Tag;
import server.configuration.MongoConfiguration;
import server.items.dropped.model.DroppedItem;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.Stacking;
import server.items.weapons.Weapon;
import server.player.character.equippable.model.EquippedItems;
import server.player.character.inventory.model.CharacterItem;
import server.player.character.inventory.model.Inventory;
import server.player.character.inventory.repository.InventoryRepository;
import server.player.character.inventory.service.InventoryService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;

@Singleton
public class ItemTestHelper {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Item> itemCollection;
    MongoCollection<DroppedItem> droppedItemCollection;
    MongoCollection<Inventory> inventoryMongoCollection;
    MongoCollection<EquippedItems> equippedItemsMongoCollection;

    @Inject
    InventoryService inventoryService;

    @Inject
    InventoryRepository inventoryRepository;

    public ItemTestHelper(
            MongoConfiguration configuration,
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

        Single.fromPublisher(
                equippedItemsMongoCollection.deleteMany(ne("characterName", "deleteAll"))
        ).blockingGet();
    }

    public static Weapon createTestWeaponItem() {
        List<Tag> weaponTags = List.of(
                new Tag("damage", "30")
        );
        Stacking stacOpts = new Stacking(false, 1, 1);
        ItemConfig itemConfig = new ItemConfig("icon", "mesh");
        return new Weapon("123", "weapon name", weaponTags, stacOpts, 1000, itemConfig);
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

    public CharacterItem addItemToInventory(Item item, String characterName) {
        Inventory inventory = getInventory(characterName);

        List<CharacterItem> items = inventory.getCharacterItems();

        CharacterItem characterItem = new CharacterItem();
        characterItem.setItem(item);
        characterItem.setCharacterName(characterName);
        characterItem.setLocation(inventoryService.getNextAvailableSlot(inventory));
        characterItem.setCharacterItemId(UUID.randomUUID().toString());

        items.add(characterItem);

        inventoryRepository.updateInventoryItems(characterName, items);

        return characterItem;
    }

    public Inventory insertInventory(Inventory inventory) {
        return Single.fromPublisher(
                inventoryMongoCollection.insertOne(inventory)
        ).map(success -> inventory).blockingGet();
    }

    public Inventory getInventory(String characterName) {
        return Single.fromPublisher(
                inventoryMongoCollection.find(
                        eq("characterName", characterName)
                )
        ).blockingGet();
    }

    private void prepareCollections() {
        this.itemCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getItemsCollection(), Item.class);

        this.droppedItemCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getDroppedItemsCollection(), DroppedItem.class);

        this.inventoryMongoCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getInventoryCollection(), Inventory.class);

        this.equippedItemsMongoCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getEquipCollection(), EquippedItems.class);
    }
}
