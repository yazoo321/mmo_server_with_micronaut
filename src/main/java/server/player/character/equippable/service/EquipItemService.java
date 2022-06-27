package server.player.character.equippable.service;

import lombok.extern.slf4j.Slf4j;
import server.items.model.Item;
import server.player.character.equippable.model.EquippedItems;
import server.player.character.equippable.model.exceptions.EquipException;
import server.player.character.equippable.repository.EquipRepository;
import server.player.character.inventory.model.CharacterItem;
import server.player.character.inventory.model.Inventory;
import server.player.character.inventory.model.exceptions.InventoryException;
import server.player.character.inventory.service.InventoryService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Slf4j
@Singleton
public class EquipItemService {

    @Inject
    EquipRepository equipRepository;

    @Inject
    InventoryService inventoryService;

    public EquippedItems equipItem(String characterItemId, String characterName) {
        // in order to equip item, first un-equip item from slot if one exists

        Inventory inventory = inventoryService.getInventory(characterName);
        List<CharacterItem> items = inventory.getCharacterItems();
        CharacterItem characterItem = items
                .stream()
                .filter(iterator->iterator.getCharacterItemId().equals(characterItemId))
                .findFirst().orElseThrow(() ->
                        new InventoryException("The item trying to equip does not exist"));

        Item item = characterItem.getItem();
        String slotType = item.getCategory();

        EquippedItems equippedItem = equipRepository.getCharacterItemSlot(characterName, slotType);

        if (equippedItem != null) {
            unequipItem(equippedItem.getCharacterItemId(), characterName);
        }

        equippedItem = item.createEquippedItem(characterName, characterItemId);
        // setting location to null effectively moves it away from inventory space
        characterItem.setLocation(null);
        inventoryService.updateInventoryItems(characterName, items);

        return equipRepository.insert(equippedItem);
    }

    public void unequipItem(String characterItemId, String characterName) {
        inventoryService.unequipItem(characterItemId, characterName);
        boolean success = equipRepository.deleteEquippedItem(characterItemId);

        if (!success) {
            log.error("error with unequip item, potential duplicate record created, characterItemId: {}",
                    characterItemId);
            throw new EquipException("Did not delete equipped item successfully");
        }
    }

    public List<EquippedItems> getEquippedItems(String characterName) {
        return equipRepository.getEquippedItemsForCharacter(characterName);
    }


}
