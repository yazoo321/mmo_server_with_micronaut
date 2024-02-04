package server.items.equippable.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import server.common.dto.Location2D;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.model.types.*;
import server.items.helper.ItemTestHelper;
import server.items.inventory.model.CharacterItem;
import server.items.inventory.model.Inventory;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.types.ItemType;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EquipItemServiceTest {

    @Inject EquipItemService equipItemService;

    @Inject ItemTestHelper itemTestHelper;

    private static final String ACTOR_ID = "test_character";

    @BeforeEach
    void cleanDb() {
        itemTestHelper.deleteAllItemData();
        itemTestHelper.prepareInventory(ACTOR_ID);
    }

    private static Stream<Arguments> itemTypesAndSlots() {
        // TODO: Support Ring slot 2, Weapon 2h, dual wield weapon
        return Stream.of(
                // Weapons
                Arguments.of(
                        ItemType.WEAPON.getType(),
                        new WeaponSlot1(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("WEAPON")))),
                Arguments.of(
                        ItemType.SHIELD.getType(),
                        new ShieldSlot(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("SHIELD")))),
                //
                //                // Accessories
                Arguments.of(
                        ItemType.BELT.getType(),
                        new BeltSlot(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("BELT")))),
                Arguments.of(
                        ItemType.CAPE.getType(),
                        new CapeSlot(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("CAPE")))),
                Arguments.of(
                        ItemType.NECK.getType(),
                        new NeckSlot(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("NECK")))),
                Arguments.of(
                        ItemType.RING.getType(),
                        new RingSlot1(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("RING")))),
                //
                //                // Armour
                Arguments.of(
                        ItemType.BOOTS.getType(),
                        new BootsSlot(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("BOOTS")))),
                Arguments.of(
                        ItemType.BRACERS.getType(),
                        new BracersSlot(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("BRACERS")))),
                Arguments.of(
                        ItemType.CHEST.getType(),
                        new ChestSlot(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("CHEST")))),
                Arguments.of(
                        ItemType.GLOVES.getType(),
                        new GlovesSlot(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("GLOVES")))),
                Arguments.of(
                        ItemType.HELM.getType(),
                        new HelmSlot(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("HELM")))),
                Arguments.of(
                        ItemType.LEGS.getType(),
                        new LegsSlot(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("LEGS")))),
                Arguments.of(
                        ItemType.SHOULDER.getType(),
                        new ShoulderSlot(
                                ACTOR_ID,
                                new ItemInstance(
                                        "not-matched",
                                        "override",
                                        ItemTestHelper.createTestItemOfType("SHOULDER")))));
    }

    @ParameterizedTest
    @MethodSource("itemTypesAndSlots")
    void equipWeaponItemWhenNothingAlreadyEquippedWillWorkAsExpected(
            String itemType, EquippedItems expectedEquipped) {
        // Given
        Item item = itemTestHelper.createAndInsertItem(itemType);
        ItemInstance instance = itemTestHelper.createItemInstanceFor(item, "override");
        CharacterItem characterItem = itemTestHelper.addItemToInventory(ACTOR_ID, instance);

        // When
        equipItemService.equipItem("override", ACTOR_ID).blockingGet();

        // Then
        List<EquippedItems> equipped = equipItemService.getEquippedItems(ACTOR_ID).blockingGet();
        Assertions.assertThat(equipped)
                .usingRecursiveComparison()
                .ignoringFields(
                        "item.itemId",
                        "itemInstance.itemId",
                        "itemInstance.item.itemId") // Because in the args list we pre-create item
                // for expected result
                .isEqualTo(List.of(expectedEquipped));

        // Then
        // Check that the inventory item has no location set

        Inventory inventory = itemTestHelper.getInventory(ACTOR_ID);
        List<CharacterItem> inventoryItems = inventory.getCharacterItems();

        CharacterItem ci1 =
                inventoryItems.stream()
                        .filter(
                                ii ->
                                        ii.getItemInstance()
                                                .getItemInstanceId()
                                                .equals(
                                                        characterItem
                                                                .getItemInstance()
                                                                .getItemInstanceId()))
                        .findFirst()
                        .get();

        Assertions.assertThat(ci1.getLocation())
                .usingRecursiveComparison()
                .isEqualTo(new Location2D(-1, -1));
    }

    @ParameterizedTest
    @MethodSource("itemTypesAndSlots")
    void equipWeaponItemWhenOneIsAlreadyEquippedWillWorkAsExpected(
            String itemType, EquippedItems expectedEquipped) {
        // Given
        Item item = itemTestHelper.createAndInsertItem(itemType);

        Item item2 = ItemTestHelper.createTestItemOfType(itemType);
        item2.setItemId(UUID.randomUUID().toString());
        item2.setItemName("some name");
        item2 = itemTestHelper.insertItem(item2);

        ItemInstance instance1 = itemTestHelper.createItemInstanceFor(item, "override2");
        ItemInstance instance2 = itemTestHelper.createItemInstanceFor(item, "override");

        CharacterItem i1 = itemTestHelper.addItemToInventory(ACTOR_ID, instance1);
        CharacterItem i2 = itemTestHelper.addItemToInventory(ACTOR_ID, instance2);

        EquippedItems items =
                equipItemService.equipItem(instance1.getItemInstanceId(), ACTOR_ID).blockingGet();

        // When
        items = equipItemService.equipItem(instance2.getItemInstanceId(), ACTOR_ID).blockingGet();

        // Then
        List<EquippedItems> equipped = equipItemService.getEquippedItems(ACTOR_ID).blockingGet();
        Assertions.assertThat(equipped.size()).isEqualTo(1);

        Assertions.assertThat(equipped)
                .usingRecursiveComparison()
                .ignoringFields(
                        "item.itemId",
                        "itemInstance.itemId",
                        "itemInstance.item.itemId") // The item was created twice in test
                .isEqualTo(List.of(expectedEquipped));

        // Then
        // also check the inventory is set as expected
        Inventory inventory = itemTestHelper.getInventory(ACTOR_ID);
        List<CharacterItem> inventoryItems = inventory.getCharacterItems();

        CharacterItem ci1 =
                inventoryItems.stream()
                        .filter(
                                ii ->
                                        ii.getItemInstance()
                                                .getItemInstanceId()
                                                .equals(i1.getItemInstance().getItemInstanceId()))
                        .findFirst()
                        .get();

        CharacterItem ci2 =
                inventoryItems.stream()
                        .filter(
                                ii ->
                                        ii.getItemInstance()
                                                .getItemInstanceId()
                                                .equals(i2.getItemInstance().getItemInstanceId()))
                        .findFirst()
                        .get();

        Assertions.assertThat(ci1.getLocation())
                .usingRecursiveComparison()
                .isEqualTo(new Location2D(0, 0));

        Assertions.assertThat(ci2.getLocation())
                .usingRecursiveComparison()
                .isEqualTo(new Location2D(-1, -1));
    }
}
