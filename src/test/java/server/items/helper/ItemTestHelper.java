package server.items.helper;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Single;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.common.dto.Tag;
import server.configuration.MongoConfiguration;
import server.items.accessories.Belt;
import server.items.accessories.Cape;
import server.items.accessories.Neck;
import server.items.accessories.Ring;
import server.items.armour.*;
import server.items.dropped.model.DroppedItem;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.ItemInstance;
import server.items.model.Stacking;
import server.items.service.ItemService;
import server.items.weapons.Shield;
import server.items.weapons.Weapon;
import server.player.character.equippable.model.EquippedItems;
import server.player.character.inventory.model.CharacterItem;
import server.player.character.inventory.model.Inventory;
import server.player.character.inventory.repository.InventoryRepository;
import server.player.character.inventory.service.InventoryService;

import javax.inject.Inject;
import javax.inject.Singleton;
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
    MongoCollection<ItemInstance> itemInstanceCollection;
    MongoCollection<DroppedItem> droppedItemCollection;
    MongoCollection<Inventory> inventoryMongoCollection;
    MongoCollection<EquippedItems> equippedItemsMongoCollection;

    @Inject
    ItemService itemService;

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

        Single.fromPublisher(
                itemInstanceCollection.deleteMany(ne("itemInstanceId", "deleteAll"))
        ).blockingGet();
    }

    public Item createAndInsertItem(String type) {
        Item item = createTestItemOfType(type);

        return insertItem(item);
    }

    public Item insertItem(Item item) {
        return Single.fromPublisher(
                itemCollection.insertOne(item)
        ).map(success -> item).blockingGet();
    }

    public ItemInstance createItemInstanceFor(Item item, String itemInstanceId) {
        ItemInstance itemInstance = new ItemInstance(item.getItemId(), itemInstanceId, item);

        return Single.fromPublisher(
                itemInstanceCollection.insertOne(itemInstance)
        ).map(success -> itemInstance).blockingGet();
    }

    public DroppedItem createAndInsertDroppedItem(Location location, Item item) {
        return itemService.createNewDroppedItem(item.getItemId(), location);
    }

    public Inventory prepareInventory(String characterName) {
        Inventory inventory = new Inventory();

        inventory.setCharacterName(characterName);
        inventory.setCharacterItems(new ArrayList<>());
        inventory.setGold(0);
        inventory.setMaxSize(new Location2D(3, 20));

        return insertInventory(inventory);
    }

    public CharacterItem addItemToInventory(String characterName, ItemInstance itemInstance) {
        Inventory inventory = getInventory(characterName);

        List<CharacterItem> items = inventory.getCharacterItems();

        CharacterItem characterItem = new CharacterItem();
        characterItem.setItemInstance(itemInstance);
        characterItem.setCharacterName(characterName);
        characterItem.setLocation(inventoryService.getNextAvailableSlot(inventory.getMaxSize(), inventory.getCharacterItems()));
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

        this.itemInstanceCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getItemInstancesCollection(), ItemInstance.class);

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

    public static Item createTestItemOfType(String type) {
        Stacking stacOpts = new Stacking(false, 1, 1);
        List<Tag> tags = new ArrayList<>();
        ItemConfig itemConfig = new ItemConfig("icon", "mesh");

        switch (type) {
            case "WEAPON":
                tags.add(new Tag("damage", "30"));
                return new Weapon(UUID.randomUUID().toString(), "sharp sword", tags, stacOpts, 1000, itemConfig);
            case "HELM":
                tags.add(new Tag("armor", "10"));
                return new Helm(UUID.randomUUID().toString(), "leather helm", tags, stacOpts, 1000, itemConfig);
            case "CHEST":
                tags.add(new Tag("armour", "10"));
                return new Chest(UUID.randomUUID().toString(), "leather armour", tags, stacOpts, 1000, itemConfig);
            case "BELT":
                tags.add(new Tag("armor", "10"));
                return new Belt(UUID.randomUUID().toString(), "leather belt", tags, stacOpts, 1000, itemConfig);
            case "BRACERS":
                tags.add(new Tag("armor", "10"));
                return new Bracers(UUID.randomUUID().toString(), "iron bracers", tags, stacOpts, 1000, itemConfig);
            case "CAPE":
                tags.add(new Tag("armor", "10"));
                return new Cape(UUID.randomUUID().toString(), "green cape", tags, stacOpts, 1000, itemConfig);
            case "GLOVES":
                tags.add(new Tag("armor", "10"));
                return new Gloves(UUID.randomUUID().toString(), "leather gloves", tags, stacOpts, 1000, itemConfig);
            case "LEGS":
                tags.add(new Tag("armor", "10"));
                return new Legs(UUID.randomUUID().toString(), "leather pants", tags, stacOpts, 1000, itemConfig);
            case "SHOULDER":
                tags.add(new Tag("armor", "10"));
                return new Shoulder(UUID.randomUUID().toString(), "leather shoulder pads", tags, stacOpts, 1000, itemConfig);
            case "NECK":
                tags.add(new Tag("armor", "10"));
                return new Neck(UUID.randomUUID().toString(), "leather shoulder pads", tags, stacOpts, 1000, itemConfig);
            case "BOOTS":
                tags.add(new Tag("armor", "10"));
                return new Boots(UUID.randomUUID().toString(), "leather boots", tags, stacOpts, 1000, itemConfig);
            case "RING":
                tags.add(new Tag("armour", "10"));
                return new Ring(UUID.randomUUID().toString(), "gold ring", tags, stacOpts, 1000, itemConfig);
            case "SHIELD":
                tags.add(new Tag("armor", "10"));
                return new Shield(UUID.randomUUID().toString(), "bronze shield", tags, stacOpts, 1000, itemConfig);
            default:
                return null;
        }

    }
}
