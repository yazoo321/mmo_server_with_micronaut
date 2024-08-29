package server.items.inventory.service;

import com.mongodb.client.result.UpdateResult;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.items.equippable.service.EquipItemService;
import server.items.inventory.model.CharacterItem;
import server.items.inventory.model.Inventory;
import server.items.inventory.model.exceptions.InventoryException;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.inventory.repository.InventoryRepository;
import server.items.model.DroppedItem;
import server.items.model.ItemInstance;
import server.items.service.ItemService;

@Slf4j
@Singleton
public class InventoryService {

    @Inject InventoryRepository inventoryRepository;
    @Inject ItemService itemService;

    public Single<Inventory> pickupItem(GenericInventoryData request) {
        return itemService
                .getDroppedItemByInstanceId(request.getItemInstanceId())
                .doOnError(e -> log.error(e.getMessage()))
                .flatMap(
                        droppedItem -> {
                            ItemInstance itemInstance = droppedItem.getItemInstance();
                            return inventoryRepository
                                    .getCharacterInventory(request.getActorId())
                                    .flatMap(
                                            inventory -> {
                                                addItemToInventory(inventory, itemInstance);
                                                // these should be chained.
                                                itemService
                                                        .deleteDroppedItem(
                                                                request.getItemInstanceId())
                                                        .blockingSubscribe();

                                                inventoryRepository
                                                        .updateInventoryItems(
                                                                request.getActorId(),
                                                                inventory.getCharacterItems())
                                                        .blockingSubscribe();

                                                return getInventory(request.getActorId());
                                            });
                        });
    }

    private void addItemToInventory(Inventory inventory, ItemInstance itemInstance) {
        // check for example if inventory is full
        List<CharacterItem> items = inventory.getCharacterItems();
        Location2D position = getNextAvailableSlot(inventory.getMaxSize(), items);

        CharacterItem newCharacterItem =
                new CharacterItem(inventory.getActorId(), position, itemInstance);

        items.add(newCharacterItem);
    }

    public Single<List<CharacterItem>> unequipItem(String itemInstanceId, String actorId) {
        // this is basically finding the nearest slot and placing item there
        return getInventory(actorId)
                .doOnError(e -> log.error("Failed to get characters inventory, {}", e.getMessage()))
                .flatMap(
                        inventory -> {
                            List<CharacterItem> items = inventory.getCharacterItems();
                            CharacterItem foundItem =
                                    EquipItemService.getCharacterItemByInstance(
                                            items, itemInstanceId);

                            Location2D loc =
                                    getNextAvailableSlot(
                                            inventory.getMaxSize(), inventory.getCharacterItems());
                            foundItem.setLocation(loc);

                            return inventoryRepository
                                    .updateInventoryItems(actorId, items)
                                    .doOnError(e -> log.error(e.getMessage()));
                        });
    }

    public Single<DroppedItem> dropItem(String actorId, String itemInstanceId, Location location)
            throws InventoryException {
        return inventoryRepository
                .getCharacterInventory(actorId)
                .doOnError(e -> log.error("Failed to get character inventory, {}", e.getMessage()))
                .flatMap(
                        inventory -> {
                            List<CharacterItem> itemsList = inventory.getCharacterItems();

                            CharacterItem item =
                                    EquipItemService.getCharacterItemByInstance(
                                            itemsList, itemInstanceId);

                            itemsList.remove(item);

                            inventoryRepository
                                    .updateInventoryItems(actorId, itemsList)
                                    .doOnError(e -> log.error(e.getMessage()))
                                    .subscribe();

                            // TODO: if dropItem fails, we need to revert the removal of item from
                            // inventory.
                            return itemService.dropExistingItem(
                                    item.getItemInstance().getItemInstanceId(), location);
                        });
    }

    public Single<Inventory> getInventory(String actorId) {
        return inventoryRepository.getCharacterInventory(actorId);
    }

    public Single<List<CharacterItem>> updateInventoryItems(
            String actorId, List<CharacterItem> characterItems) {
        return inventoryRepository.updateInventoryItems(actorId, characterItems);
    }

    public Single<Inventory> createInventoryForNewCharacter(String actorId) {
        Inventory inventory = new Inventory();

        inventory.setActorId(actorId);
        inventory.setCharacterItems(new ArrayList<>());
        inventory.setGold(0);
        inventory.setMaxSize(new Location2D(8, 8));

        return inventoryRepository.upsert(inventory);
    }

    public Single<UpdateResult> updateInventoryMaxSize(Inventory inventory) {
        return inventoryRepository.updateInventoryMaxSize(inventory);
    }

    public Location2D getNextAvailableSlot(Location2D maxSize, List<CharacterItem> items) {
        // Implement this as per your requirement, based on position for example.
        //        Location2D maxSize = inventory.getMaxSize();
        //        List<CharacterItem> items = inventory.getCharacterItems();
        int[][] invArr = new int[maxSize.getX()][maxSize.getY()];

        items.forEach(
                i -> {
                    Location2D loc = i.getLocation();
                    // process only valid locations, ignore 'equipped' items
                    if (loc != null && loc.getX() > -1) {
                        invArr[loc.getX()][loc.getY()] = 1;
                    }
                });

        for (int x = 0; x < maxSize.getY(); x++) {
            for (int y = 0; y < maxSize.getX(); y++) {
                if (invArr[x][y] != 1) {
                    return new Location2D(x, y);
                }
            }
        }

        throw new InventoryException("No available slots in inventory");
    }

    public void clearAllDataForCharacter(String actorId) {
        // This is for test purposes!
        inventoryRepository.deleteAllInventoryDataForCharacter(actorId).subscribe();
    }

    private CharacterItem getItemAtLocation(Location2D location, Inventory inventory) {
        List<CharacterItem> items = inventory.getCharacterItems();

        Optional<CharacterItem> item =
                items.stream().filter(i -> i.getLocation().equals(location)).findFirst();

        if (item.isPresent()) {
            return item.get();
        } else {
            log.warn("item was not found in the inventory");
            return null;
        }
    }
}
