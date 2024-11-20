package server.items.helper;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.*;
import server.attribute.stats.types.StatsTypes;
import server.common.configuration.MongoConfiguration;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.items.equippable.model.EquippedItems;
import server.items.inventory.model.CharacterItem;
import server.items.inventory.model.Inventory;
import server.items.inventory.repository.InventoryRepository;
import server.items.inventory.service.InventoryService;
import server.items.model.*;
import server.items.service.ItemService;
import server.items.types.accessories.Belt;
import server.items.types.accessories.Cape;
import server.items.types.accessories.Neck;
import server.items.types.accessories.Ring;
import server.items.types.armour.*;
import server.items.types.weapons.Shield;
import server.items.types.weapons.Weapon;

@Singleton
public class ItemTestHelper {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Item> itemCollection;
    MongoCollection<ItemInstance> itemInstanceCollection;
    MongoCollection<DroppedItem> droppedItemCollection;
    MongoCollection<Inventory> inventoryMongoCollection;
    MongoCollection<EquippedItems> equippedItemsMongoCollection;

    @Inject ItemService itemService;

    @Inject InventoryService inventoryService;

    @Inject InventoryRepository inventoryRepository;

    private static Map<String, Integer> defaultRequirements = Map.of(
            StatsTypes.STR.getType(), 15,
            StatsTypes.INT.getType(), 15,
            StatsTypes.DEX.getType(), 15,
            StatsTypes.STA.getType(), 15);

    public ItemTestHelper(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public void deleteAllItemData() {
        // this is for test purposes
        Single.fromPublisher(itemCollection.deleteMany(ne("category", "deleteAll"))).blockingGet();

        Single.fromPublisher(droppedItemCollection.deleteMany(ne("map", "deleteAll")))
                .blockingGet();

        Single.fromPublisher(inventoryMongoCollection.deleteMany(ne("actorId", "deleteAll")))
                .blockingGet();

        Single.fromPublisher(equippedItemsMongoCollection.deleteMany(ne("actorId", "deleteAll")))
                .blockingGet();

        Single.fromPublisher(itemInstanceCollection.deleteMany(ne("itemInstanceId", "deleteAll")))
                .blockingGet();
    }

    public Item createAndInsertItem(String type) {
        Item item = createTestItemOfType(type);

        return insertItem(item);
    }

    public Item insertItem(Item item) {
        return Single.fromPublisher(itemCollection.insertOne(item))
                .map(success -> item)
                .blockingGet();
    }

    public ItemInstance createItemInstanceFor(Item item, String itemInstanceId) {
        ItemInstance itemInstance = new ItemInstance(item.getItemId(), itemInstanceId, item);

        return Single.fromPublisher(itemInstanceCollection.insertOne(itemInstance))
                .map(success -> itemInstance)
                .blockingGet();
    }

    public DroppedItem createAndInsertDroppedItem(Location location, Item item) {
        return itemService.createNewDroppedItem(item.getItemId(), location).blockingGet();
    }

    public Inventory prepareInventory(String actorId) {
        Inventory inventory = new Inventory();

        inventory.setActorId(actorId);
        inventory.setCharacterItems(new ArrayList<>());
        inventory.setGold(0);
        inventory.setMaxSize(new Location2D(3, 20));

        return insertInventory(inventory);
    }

    public CharacterItem addItemToInventory(String actorId, ItemInstance itemInstance) {
        Inventory inventory = getInventory(actorId);

        List<CharacterItem> items = inventory.getCharacterItems();

        CharacterItem characterItem = new CharacterItem();
        characterItem.setItemInstance(itemInstance);
        characterItem.setActorId(actorId);
        characterItem.setLocation(inventory.getNextAvailableSlot());
        items.add(characterItem);

        inventoryRepository.updateInventoryItems(actorId, items).blockingSubscribe();

        return characterItem;
    }

    public Inventory insertInventory(Inventory inventory) {
        return Single.fromPublisher(inventoryMongoCollection.insertOne(inventory))
                .map(success -> inventory)
                .blockingGet();
    }

    public Inventory getInventory(String actorId) {
        return Single.fromPublisher(inventoryMongoCollection.find(eq("actorId", actorId)))
                .blockingGet();
    }

    private void prepareCollections() {
        this.itemCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getItemsCollection(), Item.class);

        this.itemInstanceCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(
                                configuration.getItemInstancesCollection(), ItemInstance.class);

        this.droppedItemCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(
                                configuration.getDroppedItemsCollection(), DroppedItem.class);

        this.inventoryMongoCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getInventoryCollection(), Inventory.class);

        this.equippedItemsMongoCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getEquipCollection(), EquippedItems.class);
    }

    public static Item createTestItemOfType(String type) {
        Stacking stacOpts = new Stacking(false, 1, 1);
        Map<String, Double> itemEffects = new HashMap<>();
        ItemConfig itemConfig = new ItemConfig("icon", "mesh");

        switch (type) {
            case "WEAPON":
                itemEffects.put(StatsTypes.WEAPON_DAMAGE.getType(), 300.0);
                itemEffects.put(StatsTypes.BASE_ATTACK_SPEED.getType(), 0.2);

                return new Weapon(
                        UUID.randomUUID().toString(),
                        "sharp sword",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            case "HELM":
                itemEffects.put(StatsTypes.DEF.getType(), 20.0);
                return new Helm(
                        UUID.randomUUID().toString(),
                        "leather helm",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            case "CHEST":
                itemEffects.put(StatsTypes.DEF.getType(), 20.0);
                return new Chest(
                        UUID.randomUUID().toString(),
                        "leather armour",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            case "BELT":
                itemEffects.put(StatsTypes.DEF.getType(), 20.0);
                return new Belt(
                        UUID.randomUUID().toString(),
                        "leather belt",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            case "BRACERS":
                itemEffects.put(StatsTypes.DEF.getType(), 20.0);
                return new Bracers(
                        UUID.randomUUID().toString(),
                        "iron bracers",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            case "CAPE":
                itemEffects.put(StatsTypes.DEF.getType(), 20.0);
                return new Cape(
                        UUID.randomUUID().toString(),
                        "green cape",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            case "GLOVES":
                itemEffects.put(StatsTypes.DEF.getType(), 20.0);
                return new Gloves(
                        UUID.randomUUID().toString(),
                        "leather gloves",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            case "LEGS":
                itemEffects.put(StatsTypes.DEF.getType(), 20.0);
                return new Legs(
                        UUID.randomUUID().toString(),
                        "leather pants",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            case "SHOULDER":
                itemEffects.put(StatsTypes.DEF.getType(), 20.0);
                return new Shoulder(
                        UUID.randomUUID().toString(),
                        "leather shoulder pads",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            case "NECK":
                itemEffects.put(StatsTypes.DEF.getType(), 20.0);
                return new Neck(
                        UUID.randomUUID().toString(),
                        "leather shoulder pads",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            case "BOOTS":
                itemEffects.put(StatsTypes.DEF.getType(), 20.0);
                return new Boots(
                        UUID.randomUUID().toString(),
                        "leather boots",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            case "RING":
                itemEffects.put(StatsTypes.DEF.getType(), 20.0);
                return new Ring(
                        UUID.randomUUID().toString(),
                        "gold ring",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            case "SHIELD":
                itemEffects.put(StatsTypes.DEF.getType(), 20.0);
                return new Shield(
                        UUID.randomUUID().toString(),
                        "bronze shield",
                        itemEffects,
                        defaultRequirements,
                        1,
                        stacOpts,
                        1000,
                        itemConfig);
            default:
                return null;
        }
    }
}
