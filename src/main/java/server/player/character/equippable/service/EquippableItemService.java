package server.player.character.equippable.service;

import server.items.model.Item;
import server.player.character.equippable.model.EquippableSlots;
import server.player.character.equippable.model.EquippedItems;
import server.player.character.equippable.repository.EquipRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class EquippableItemService {

    @Inject
    EquipRepository equipRepository;

    public void setupEquippedItemsForNewCharacter(String characterName) {

        EquippedItems equippedItems = new EquippedItems();

        equippedItems.setCharacterName(characterName);
        equippedItems.setEquippedSlots(new HashMap<>());

        equipRepository.insert(equippedItems);
    }

    public void tryEquipItem(Item item, String characterName) {
        // this is trivial, automated equip item functionality.
        // e.g. double click item, figure out where to equip it and do it
        EquippedItems items = equipRepository.getEquippedItemsForCharacter(characterName);
        Map<String, EquippableSlots> slots = items.getEquippedSlots();

        List<String> validTypes = item.getValidSlotTypes().stream().map(s->s.type).collect(Collectors.toList());

        if (slots.isEmpty()) {
            // there's no item equipped in that slot, so just create new one

        }

    }

    private void unequipItem(Item) {

    }

    private void equipItem(Item item, EquippableSlots slot) {
        if (slot == null) {
            slot = new EquippableSlots()
        }
    }




}
