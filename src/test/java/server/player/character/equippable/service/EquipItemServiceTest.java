package server.player.character.equippable.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import server.items.helper.ItemTestHelper;
import server.items.model.Item;
import server.items.types.ItemType;
import server.items.weapons.Weapon;
import server.player.character.equippable.model.EquippedItems;
import server.player.character.equippable.model.types.WeaponSlot1;
import server.player.character.inventory.model.CharacterItem;
import server.player.character.inventory.service.InventoryService;

import javax.inject.Inject;
import java.util.List;

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

    @Test
    void equipWeaponItemWhenNothingAlreadyEquippedWillWorkAsExpected() {
        // Given
        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());
        CharacterItem item  = itemTestHelper.addItemToInventory(weapon, CHARACTER_NAME);

        // When
        equipItemService.equipItem(item, CHARACTER_NAME);

        // Then
        List<EquippedItems> equipped = equipItemService.getEquippedItems(CHARACTER_NAME);
        WeaponSlot1 expectedWeapon = new WeaponSlot1(CHARACTER_NAME, item.getCharacterItemId());
        Assertions.assertThat(equipped).usingRecursiveComparison().isEqualTo(List.of(expectedWeapon));
    }

    @Test
    void equipWeaponItemWhenOneIsAlreadyEquippedWillWorkAsExpected() {
        // Given
        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());

        Weapon weapon2 = (Weapon) ItemTestHelper.createTestItemOfType(ItemType.WEAPON.getType());
        weapon2.setItemId("111");
        weapon2.setItemName("hammer");
        weapon2 = (Weapon) itemTestHelper.insertItem(weapon2);

        CharacterItem i1  = itemTestHelper.addItemToInventory(weapon, CHARACTER_NAME);
        CharacterItem i2  = itemTestHelper.addItemToInventory(weapon2, CHARACTER_NAME);

        equipItemService.equipItem(i1, CHARACTER_NAME);

        // When
        equipItemService.equipItem(i2, CHARACTER_NAME);

        // Then
        List<EquippedItems> equipped = equipItemService.getEquippedItems(CHARACTER_NAME);
        Assertions.assertThat(equipped.size()).isEqualTo(1);

        WeaponSlot1 expectedWeapon = new WeaponSlot1(CHARACTER_NAME, i2.getCharacterItemId());
        Assertions.assertThat(equipped).usingRecursiveComparison().isEqualTo(List.of(expectedWeapon));
    }

    @Test
    void equipHelmItemWhenNothingAlreadyEquippedWillWorkAsExpected() {
        // Given
        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());
        CharacterItem item  = itemTestHelper.addItemToInventory(weapon, CHARACTER_NAME);

        // When
        equipItemService.equipItem(item, CHARACTER_NAME);

        // Then
        List<EquippedItems> equipped = equipItemService.getEquippedItems(CHARACTER_NAME);
        WeaponSlot1 expectedWeapon = new WeaponSlot1(CHARACTER_NAME, item.getCharacterItemId());
        Assertions.assertThat(equipped).usingRecursiveComparison().isEqualTo(List.of(expectedWeapon));
    }

    @Test
    void equipHelmItemWhenOneIsAlreadyEquippedWillWorkAsExpected() {
        // Given
        Weapon weapon = (Weapon) itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());

        Weapon weapon2 = (Weapon) ItemTestHelper.createTestItemOfType(ItemType.WEAPON.getType());
        weapon2.setItemId("111");
        weapon2.setItemName("hammer");
        weapon2 = (Weapon) itemTestHelper.insertItem(weapon2);

        CharacterItem i1  = itemTestHelper.addItemToInventory(weapon, CHARACTER_NAME);
        CharacterItem i2  = itemTestHelper.addItemToInventory(weapon2, CHARACTER_NAME);

        equipItemService.equipItem(i1, CHARACTER_NAME);

        // When
        equipItemService.equipItem(i2, CHARACTER_NAME);

        // Then
        List<EquippedItems> equipped = equipItemService.getEquippedItems(CHARACTER_NAME);
        Assertions.assertThat(equipped.size()).isEqualTo(1);

        WeaponSlot1 expectedWeapon = new WeaponSlot1(CHARACTER_NAME, i2.getCharacterItemId());
        Assertions.assertThat(equipped).usingRecursiveComparison().isEqualTo(List.of(expectedWeapon));
    }
}
