package server.player.character.equippable.service;

import lombok.extern.slf4j.Slf4j;
import server.common.dto.Tag;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.repository.ItemRepository;
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
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class EquipItemService {

    @Inject
    EquipRepository equipRepository;

    @Inject
    InventoryService inventoryService;

    @Inject
    ItemRepository itemRepository;

    public EquippedItems equipItem(String itemInstanceId, String characterName) {
        // in order to equip item, first un-equip item from slot if one exists

        Inventory inventory = inventoryService.getInventory(characterName);
        List<CharacterItem> items = inventory.getCharacterItems();
        CharacterItem characterItem = items
                .stream()
                .filter(iterator->iterator.getItemInstance().getItemInstanceId().equals(itemInstanceId))
                .findFirst().orElseThrow(() ->
                        new InventoryException("The item trying to equip does not exist"));

        ItemInstance instance = characterItem.getItemInstance();
        Item item = instance.getItem();

        String slotType = item.getCategory();

        EquippedItems equippedItem = equipRepository.getCharacterItemSlot(characterName, slotType);

        if (equippedItem != null) {
            unequipItem(equippedItem.getItemInstance().getItemInstanceId(), characterName);
        }

        equippedItem = item.createEquippedItem(characterName, instance);
        // setting location to null effectively moves it away from inventory space
        characterItem.setLocation(null);
        inventoryService.updateInventoryItems(characterName, items);

        return equipRepository.insert(equippedItem);
    }

    public Inventory unequipItem(String itemInstanceId, String characterName) {
        inventoryService.unequipItem(itemInstanceId, characterName);
        boolean success = equipRepository.deleteEquippedItem(itemInstanceId);

        if (!success) {
            log.error("error with unequip item, potential duplicate record created, itemInstanceId: {}",
                    itemInstanceId);
            throw new EquipException("Did not delete equipped item successfully");
        }

        return inventoryService.getInventory(characterName);
    }

    public List<EquippedItems> getEquippedItems(String characterName) {
        return equipRepository.getEquippedItemsForCharacter(characterName);
    }

    public void getModifiersOfEquippedItems(String characterName) {
        List<EquippedItems> equippedItems = equipRepository.getEquippedItemsForCharacter(characterName);

        List<ItemInstance> itemInstances = equippedItems.stream().map(EquippedItems::getItemInstance)
                .collect(Collectors.toList());

        // TBD
    }


}
