package server.player.character.equippable.service;

import lombok.extern.slf4j.Slf4j;
import server.items.model.Item;
import server.player.character.equippable.model.EquippedItems;
import server.player.character.equippable.model.exceptions.EquipException;
import server.player.character.equippable.model.types.WeaponSlot1;
import server.player.character.equippable.repository.EquipRepository;
import server.player.character.inventory.model.CharacterItem;
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

    public EquippedItems equipItem(CharacterItem itemToEquip, String characterName) {
        if (!characterName.equalsIgnoreCase(itemToEquip.getCharacterName())) {
            log.error("equipItem: Request came from unexpected character, potential hacking in progress");
            return null;
        }

        // in order to equip item, first un-equip item from slot if one exists
        Item item = itemToEquip.getItem();
        String slotType = item.getCategory();

        EquippedItems equippedItem = equipRepository.getCharacterItemSlot(characterName, slotType);

        if (equippedItem != null) {
            unequipItem(equippedItem, characterName);
        }

        equippedItem = item.createEquippedItem(characterName, itemToEquip.getCharacterItemId());
        return equipRepository.insert(equippedItem);
    }

    public void unequipItem(EquippedItems item, String characterName) {
        if (!item.getCharacterName().equalsIgnoreCase(characterName)) {
            log.error("unequipItem: Request came from unexpected character, potential hacking in progress");
        }
        inventoryService.unequipItem(item.getCharacterItemId(), item.getCharacterName());
        boolean success = equipRepository.deleteEquippedItem(item.getCharacterItemId());

        if (!success) {
            log.error("error with unequip item, potential duplicate record created, characterItemId: {}",
                    item.getCharacterItemId());
            throw new EquipException("Did not delete equipped item successfully");
        }
    }

    public List<EquippedItems> getEquippedItems(String characterName) {
        return equipRepository.getEquippedItemsForCharacter(characterName);
    }


}
