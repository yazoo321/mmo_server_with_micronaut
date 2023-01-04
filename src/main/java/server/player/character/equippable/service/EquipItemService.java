package server.player.character.equippable.service;

import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location2D;
import server.items.model.ItemInstance;
import server.items.repository.ItemRepository;
import server.player.character.equippable.model.EquippedItems;
import server.player.character.equippable.model.exceptions.EquipException;
import server.player.character.equippable.repository.EquipRepository;
import server.player.character.inventory.model.CharacterItem;
import server.player.character.inventory.model.Inventory;
import server.player.character.inventory.model.exceptions.InventoryException;
import server.player.character.inventory.service.InventoryService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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

        ItemInstance instance = getCharacterItemByInstance(items, itemInstanceId).getItemInstance();

        String slotType = instance.getItem().getCategory();

        EquippedItems equippedItem = equipRepository.getCharacterItemSlot(characterName, slotType);

        if (equippedItem != null) {
           items = unequipItem(equippedItem.getItemInstance().getItemInstanceId(), characterName).getCharacterItems();
        }

        // the items object has been refreshed, need to re-sync object
        CharacterItem characterItem = getCharacterItemByInstance(items, itemInstanceId);

        equippedItem = characterItem.getItemInstance().getItem().createEquippedItem(characterName, instance);

        // setting location to 'invalid' value, to not show it in inventory
        characterItem.setLocation(new Location2D(-1, -1));
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

    private CharacterItem getCharacterItemByInstance(List<CharacterItem> items, String itemInstanceId) {
        return items
                .stream()
                .filter(iterator->iterator.getItemInstance().getItemInstanceId().equals(itemInstanceId))
                .findFirst().orElseThrow(() ->
                new InventoryException("The item trying to equip does not exist"));
    }

    public void getModifiersOfEquippedItems(String characterName) {
        List<EquippedItems> equippedItems = equipRepository.getEquippedItemsForCharacter(characterName);

        List<ItemInstance> itemInstances = equippedItems.stream().map(EquippedItems::getItemInstance)
                .collect(Collectors.toList());

        // TBD
    }


}
