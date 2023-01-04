package server.player.character.inventory.service;

import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.items.dropped.model.DroppedItem;
import server.items.model.ItemInstance;
import server.items.repository.ItemRepository;
import server.items.service.ItemService;
import server.player.character.equippable.model.exceptions.EquipException;
import server.player.character.inventory.model.CharacterItem;
import server.player.character.inventory.model.Inventory;
import server.player.character.inventory.model.exceptions.InventoryException;
import server.player.character.inventory.repository.InventoryRepository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
public class InventoryService {

    @Inject
    InventoryRepository inventoryRepository;

    @Inject
    ItemService itemService;

    @Inject
    ItemRepository itemRepository;

    public Inventory pickupItem(String characterName, String droppedItemId) throws InventoryException {
        // Could add additional validations.
        // For example add unique ID to player items and match it with dropped ID
        // There can be occasions where when laggy
        // you could add item more than once / to multiple users

        DroppedItem droppedItem = itemService.getDroppedItemById(droppedItemId);
        ItemInstance instance = droppedItem.getItemInstance();

        Inventory inventory = inventoryRepository.getCharacterInventory(characterName);

        // check for example if inventory is full
        List<CharacterItem> items = inventory.getCharacterItems();
        Location2D position = getNextAvailableSlot(inventory.getMaxSize(), inventory.getCharacterItems());

        if (position == null) {
            // no inventory slots left
            throw new InventoryException("No available slots in inventory");
        }

        CharacterItem newCharacterItem = new CharacterItem(characterName, position, instance);

        items.add(newCharacterItem);

        // delete the dropped item first (this is a blocking get) to prevent duplication
        itemService.deleteDroppedItem(droppedItemId);

        inventoryRepository.updateInventoryItems(characterName, items);

        return getInventory(characterName);
    }

    public List<CharacterItem> unequipItem(String itemInstanceId, String characterName) {
        // this is basically finding the nearest slot and placing item there
        Inventory inventory = getInventory(characterName);
        Location2D loc = getNextAvailableSlot(inventory.getMaxSize(), inventory.getCharacterItems());

        if (loc == null) {
            throw new EquipException("Inventory full to unequip item");
        }

        List<CharacterItem> items = inventory.getCharacterItems();

        CharacterItem foundItem = items.stream()
                .filter(i -> i.getItemInstance().getItemInstanceId()
                .equals(itemInstanceId))
                .findFirst()
                .orElse(null);

        if (foundItem == null) {
            // this is unexpected, the item should exist in the inventory
            log.error("Un-equip item unexpectedly failed for character {} and itemInstanceId {}",
                    characterName, itemInstanceId);
            throw new EquipException("Un-equip has unexpectedly failed");
        }

        foundItem.setLocation(loc);
        inventoryRepository.updateInventoryItems(characterName, items);

        return items;
    }

    public DroppedItem dropItem(String characterName, Location2D inventoryLocation, Location location)
            throws InventoryException {
        Inventory inventory = inventoryRepository.getCharacterInventory(characterName);
        CharacterItem characterItem = getItemAtLocation(inventoryLocation, inventory);

        if (characterItem == null) {
            return null;
        }

        List<CharacterItem> itemsList = inventory.getCharacterItems();
        itemsList.remove(characterItem);
        inventoryRepository.updateInventoryItems(characterName, itemsList);

        // TODO: if dropItem fails, we need to revert the removal of item from inventory.
        return itemService.dropExistingItem(characterItem.getItemInstance().getItemInstanceId(), location);
    }

    public Inventory getInventory(String characterName) {
        return inventoryRepository.getCharacterInventory(characterName);
    }

    public void updateInventoryItems(String characterName, List<CharacterItem> characterItems) {
        inventoryRepository.updateInventoryItems(characterName, characterItems);
    }

    public Inventory createInventoryForNewCharacter(String characterName) {
        Inventory inventory = new Inventory();

        inventory.setCharacterName(characterName);
        inventory.setCharacterItems(new ArrayList<>());
        inventory.setGold(0);
        inventory.setMaxSize(new Location2D(4, 10));

        return inventoryRepository.insert(inventory);
    }

    public void updateInventoryMaxSize(Inventory inventory) {
        inventoryRepository.updateInventoryMaxSize(inventory);
    }

    public Location2D getNextAvailableSlot(Location2D maxSize, List<CharacterItem> items) {
        // Implement this as per your requirement, based on position for example.
//        Location2D maxSize = inventory.getMaxSize();
//        List<CharacterItem> items = inventory.getCharacterItems();
        int[][] invArr = new int[maxSize.getX()][maxSize.getY()];

        items.forEach(i -> {
            Location2D loc = i.getLocation();
            // process only valid locations, ignore 'equipped' items
            if (loc != null && loc.getX() > -1) {
                invArr[loc.getX()][loc.getY()] = 1;
            }
        });

        for (int x = 0; x < maxSize.getY(); x++) {
            for (int y = 0; y < maxSize.getX(); y++) {
                if (invArr[x][y] != 1) {
                    return new Location2D(x,y);
                }
            }
        }

        return null;
    }

    public void clearAllDataForCharacter(String characterName) {
        // This is for test purposes!
        inventoryRepository.deleteAllInventoryDataForCharacter(characterName);
    }

    private CharacterItem getItemAtLocation(Location2D location, Inventory inventory) {
        List<CharacterItem> items = inventory.getCharacterItems();

        Optional<CharacterItem> item = items.stream().filter(i -> i.getLocation().equals(location)).findFirst();

        if (item.isPresent()) {
            return item.get();
        } else {
            log.warn("item was not found in the inventory");
            return null;
        }
    }
}
