package server.player.character.inventory.service;

import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.items.dropped.model.DroppedItem;
import server.items.dto.Item;
import server.items.service.ItemService;
import server.player.character.inventory.model.CharacterItem;
import server.player.character.inventory.model.Inventory;
import server.player.character.inventory.model.exceptions.InventoryException;
import server.player.character.inventory.repository.InventoryRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Slf4j
@Singleton
public class InventoryService {

    @Inject
    InventoryRepository inventoryRepository;

    @Inject
    ItemService itemService;

    public Inventory pickupItem(String characterName, DroppedItem droppedItem) throws InventoryException {
        // Could add additional validations.
        // For example add unique ID to player items and match it with dropped ID
        // There can be occasions where when laggy
        // you could add item more than once / to multiple users

        Item item = droppedItem.getItem();
        Inventory inventory = inventoryRepository.getCharacterInventory(characterName);

        // check for example if inventory is full
        List<CharacterItem> items = inventory.getCharacterItems();
        Location2D position = getNextAvailableSlot(inventory);

        CharacterItem newCharacterItem = new CharacterItem(characterName, item, position);

        items.add(newCharacterItem);

        // delete the dropped item first (this is a blocking get) to prevent duplication
        itemService.deleteDroppedItem(droppedItem);

        inventoryRepository.updateInventoryItems(characterName, items);

        return inventory;
    }

    public DroppedItem dropItem(String characterName, CharacterItem characterItem, Location location) throws InventoryException {
        if (!characterName.equals(characterItem.getCharacterName())) {
            // problem - potential hacking
            log.warn("Drop item request received but character name mismatch");
            return null;
        }
        Inventory inventory = inventoryRepository.getCharacterInventory(characterName);
        inventoryRepository.removeItemFromInventory(inventory, characterItem);

        return itemService.dropItem(characterItem.getItem(), location);
    }

    public Inventory getInventory(String characterName) {
        return inventoryRepository.getCharacterInventory(characterName);
    }

    public void sellItem() {
        // TODO: later
    }

    public void equipItem() {
        // validate item and target
        // TODO: later
    }

    public void pickupGold() {
        // TODO: later
    }


    private Location2D getNextAvailableSlot(Inventory inventory) {
        // Implement this as per your requirement, based on position for example.
        Location2D maxSize = inventory.getMaxSize();
        List<CharacterItem> items = inventory.getCharacterItems();
        Integer[][] invArr = new Integer[maxSize.getX()][maxSize.getY()];

        items.forEach(i -> {
            Location2D loc = i.getLocation();
            invArr[loc.getX()][loc.getY()] = 1;
        });

        for (int x = 0; x < maxSize.getX(); x++) {
            for (int y = 0; y < maxSize.getY(); y++) {
                if (invArr[x][y] != 1) {
                    return new Location2D(x,y);
                }
            }
        }

        return null;
    }
}
