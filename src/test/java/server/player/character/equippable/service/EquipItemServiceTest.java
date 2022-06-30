package server.player.character.equippable.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import server.items.helper.ItemTestHelper;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.types.ItemType;
import server.player.character.equippable.model.EquippedItems;
import server.player.character.equippable.model.types.*;
import server.player.character.inventory.model.CharacterItem;
import server.player.character.inventory.service.InventoryService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EquipItemServiceTest {

    @Inject
    EquipItemService equipItemService;

    @Inject
    InventoryService inventoryService;

    @Inject
    ItemTestHelper itemTestHelper;

    private static final String CHARACTER_NAME = "test_character";

    @BeforeEach
    void cleanDb() {
        itemTestHelper.deleteAllItemData();
        itemTestHelper.prepareInventory(CHARACTER_NAME);
    }

    private static Stream<Arguments> itemTypesAndSlots() {
        // TODO: Support Ring slot 2, Weapon 2h, dual wield weapon
        return Stream.of(
                // Weapons
                Arguments.of(ItemType.WEAPON.getType(), new WeaponSlot1(CHARACTER_NAME, "override")),
                Arguments.of(ItemType.SHIELD.getType(), new ShieldSlot(CHARACTER_NAME, "override")),

                // Accessories
                Arguments.of(ItemType.BELT.getType(), new BeltSlot(CHARACTER_NAME, "override")),
                Arguments.of(ItemType.CAPE.getType(), new CapeSlot(CHARACTER_NAME, "override")),
                Arguments.of(ItemType.NECK.getType(), new NeckSlot(CHARACTER_NAME, "override")),
                Arguments.of(ItemType.RING.getType(), new RingSlot1(CHARACTER_NAME, "override")),

                // Armour
                Arguments.of(ItemType.BOOTS.getType(), new BootsSlot(CHARACTER_NAME, "override")),
                Arguments.of(ItemType.BRACERS.getType(), new BracersSlot(CHARACTER_NAME, "override")),
                Arguments.of(ItemType.CHEST.getType(), new ChestSlot(CHARACTER_NAME, "override")),
                Arguments.of(ItemType.GLOVES.getType(), new GlovesSlot(CHARACTER_NAME, "override")),
                Arguments.of(ItemType.HELM.getType(), new HelmSlot(CHARACTER_NAME, "override")),
                Arguments.of(ItemType.LEGS.getType(), new LegsSlot(CHARACTER_NAME, "override")),
                Arguments.of(ItemType.SHOULDER.getType(), new ShoulderSlot(CHARACTER_NAME, "override"))
            );
    }

    @ParameterizedTest
    @MethodSource("itemTypesAndSlots")
    void equipWeaponItemWhenNothingAlreadyEquippedWillWorkAsExpected(String itemType, EquippedItems expectedEquipped) {
        // Given
        Item item = itemTestHelper.createAndInsertItem(itemType);
        String itemInstanceId = "override";
        ItemInstance instance = itemTestHelper.createItemInstanceFor(item.getItemId(), itemInstanceId, new ArrayList<>());
        CharacterItem characterItem = itemTestHelper.addItemToInventory(CHARACTER_NAME, itemInstanceId);

        // When
        equipItemService.equipItem(characterItem.getItemInstanceId(), CHARACTER_NAME);

        // Then
        List<EquippedItems> equipped = equipItemService.getEquippedItems(CHARACTER_NAME);
        Assertions.assertThat(equipped).usingRecursiveComparison().isEqualTo(List.of(expectedEquipped));
    }

    @ParameterizedTest
    @MethodSource("itemTypesAndSlots")
    void equipWeaponItemWhenOneIsAlreadyEquippedWillWorkAsExpected(String itemType, EquippedItems expectedEquipped) {
        // Given
        Item item = itemTestHelper.createAndInsertItem(itemType);

        Item item2 = ItemTestHelper.createTestItemOfType(itemType);
        item2.setItemId(UUID.randomUUID().toString());
        item2.setItemName("some name");
        item2 = itemTestHelper.insertItem(item2);

        ItemInstance instance1 = itemTestHelper.createItemInstanceFor(item.getItemId(), "override2", new ArrayList<>());
        ItemInstance instance2 = itemTestHelper.createItemInstanceFor(item.getItemId(), "override", new ArrayList<>());

        CharacterItem i1  = itemTestHelper.addItemToInventory(CHARACTER_NAME, "override2");
        CharacterItem i2  = itemTestHelper.addItemToInventory(CHARACTER_NAME, "override");

        equipItemService.equipItem(i1.getItemInstanceId(), CHARACTER_NAME);

        // When
        equipItemService.equipItem(i2.getItemInstanceId(), CHARACTER_NAME);

        // Then
        List<EquippedItems> equipped = equipItemService.getEquippedItems(CHARACTER_NAME);
        Assertions.assertThat(equipped.size()).isEqualTo(1);

        Assertions.assertThat(equipped).usingRecursiveComparison().isEqualTo(List.of(expectedEquipped));
    }

}
