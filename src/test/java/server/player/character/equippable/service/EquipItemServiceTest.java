package server.player.character.equippable.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import server.items.helper.ItemTestHelper;
import server.items.model.Item;
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
        Weapon weapon = itemTestHelper.createAndInsertWeaponItem();
        CharacterItem item  = itemTestHelper.addItemToInventory(weapon, CHARACTER_NAME);

        // When
        equipItemService.equipItem(item, CHARACTER_NAME);

        // Then
        List<EquippedItems> equipped = equipItemService.getEquippedItems(CHARACTER_NAME);
        WeaponSlot1 expectedWeapon = new WeaponSlot1(CHARACTER_NAME, item.getCharacterItemId());
        Assertions.assertThat(equipped).usingRecursiveComparison().isEqualTo(List.of(expectedWeapon));
    }

}
