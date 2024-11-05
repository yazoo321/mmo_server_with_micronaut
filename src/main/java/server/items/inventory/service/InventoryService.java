package server.items.inventory.service;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
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
        log.info(
                "Requesting to pickup item, actor: {}, item instance: {}",
                request.getActorId(),
                request.getItemInstanceId());
        return itemService
                .getDroppedItemByInstanceId(request.getItemInstanceId())
                .doOnError(e -> log.error("Failed to get dropped item: {}", e.getMessage()))
                .flatMap(
                        droppedItem -> {
                            ItemInstance itemInstance = droppedItem.getItemInstance();
                            return inventoryRepository
                                    .getCharacterInventory(request.getActorId())
                                    .flatMap(
                                            inventory -> {
                                                addItemToInventory(inventory, itemInstance);

                                                // Chain the deletion of the dropped item and
                                                // inventory update using flatMapCompletable
                                                return itemService
                                                        .deleteDroppedItem(
                                                                request.getItemInstanceId())
                                                        .flatMap(
                                                                deletedResult ->
                                                                        inventoryRepository
                                                                                .updateInventoryItems(
                                                                                        request
                                                                                                .getActorId(),
                                                                                        inventory
                                                                                                .getCharacterItems()))
                                                        .flatMap(
                                                                characterItemList ->
                                                                        Single.just(inventory));
                                            });
                        })
                .onErrorResumeNext(
                        e -> {
                            log.error("Error during item pickup: {}", e.getMessage());
                            return Single.error(new InventoryException("Failed to pick up item"));
                        });
    }

    private void addItemToInventory(Inventory inventory, ItemInstance itemInstance) {
        log.info(
                "Adding item to inventory, actor id: {}, item instance id: {}",
                inventory.getActorId(),
                itemInstance.getItemInstanceId());
        // check for example if inventory is full
        List<CharacterItem> items = inventory.getCharacterItems();
        Location2D position = inventory.getNextAvailableSlot();

        CharacterItem newCharacterItem =
                new CharacterItem(inventory.getActorId(), position, itemInstance);

        items.add(newCharacterItem);
    }

    public Single<Inventory> moveItem(String actorId, String itemInstanceId, Location2D to) {
        log.info(
                "moving item, actor id: {}, item instance id: {}, location: {}",
                actorId,
                itemInstanceId,
                to);
        return getInventory(actorId)
                .doOnError(
                        er -> {
                            log.error(er.getMessage());
                            throw new InventoryException("Failed to move item, item not found");
                        })
                .map(
                        inventory -> {
                            CharacterItem movingItem =
                                    inventory.getItemByInstanceId(itemInstanceId);
                            CharacterItem itemAtLocation = inventory.getItemAtLocation(to);
                            // if item exists at location, let's swap their locations

                            if (movingItem == null) {
                                log.error("error moving item, failed to find item to move");

                                throw new InventoryException("Failed to move item, item not found");
                            }

                            if (itemAtLocation != null) {
                                itemAtLocation.setLocation(movingItem.getLocation());
                            }

                            movingItem.setLocation(to);

                            inventoryRepository
                                    .updateInventoryItems(actorId, inventory.getCharacterItems())
                                    .subscribe();

                            return inventory;
                        });
    }

    public Single<List<CharacterItem>> unequipItem(String itemInstanceId, String actorId) {
        log.info("un equipping item, actor id: {}, item instance id: {}", actorId, itemInstanceId);
        // this is basically finding the nearest slot and placing item there
        return getInventory(actorId)
                .doOnError(e -> log.error("Failed to get characters inventory, {}", e.getMessage()))
                .flatMap(
                        inventory -> {
                            List<CharacterItem> items = inventory.getCharacterItems();
                            CharacterItem foundItem =
                                    EquipItemService.getCharacterItemByInstance(
                                            items, itemInstanceId);

                            Location2D loc = inventory.getNextAvailableSlot();
                            foundItem.setLocation(loc);

                            return inventoryRepository
                                    .updateInventoryItems(actorId, items)
                                    .doOnError(e -> log.error(e.getMessage()));
                        });
    }

    public Single<DroppedItem> dropItem(String actorId, String itemInstanceId, Location location)
            throws InventoryException {
        log.info(
                "Requesting to drop item, actor id: {}, item instance id: {}, location: {}",
                actorId,
                itemInstanceId,
                location);
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
        log.info("Fetching inventory for actor: {}", actorId);
        return inventoryRepository.getCharacterInventory(actorId);
    }

    public Single<List<CharacterItem>> updateInventoryItems(
            String actorId, List<CharacterItem> characterItems) {
        log.info("updating inventory items for actor: {}", actorId);
        return inventoryRepository.updateInventoryItems(actorId, characterItems);
    }

    public Single<Inventory> createInventoryForNewCharacter(String actorId) {
        log.info("creating inventory for actor: {}", actorId);
        Inventory inventory = new Inventory();

        inventory.setActorId(actorId);
        inventory.setCharacterItems(new ArrayList<>());
        inventory.setGold(0);
        inventory.setMaxSize(new Location2D(8, 8));

        return inventoryRepository.upsert(inventory);
    }

    public Single<UpdateResult> updateInventoryMaxSize(Inventory inventory) {
        log.info(
                "updating inventory max size for actor: {}, new max size: {}",
                inventory.getActorId(),
                inventory.getMaxSize());
        return inventoryRepository.updateInventoryMaxSize(inventory);
    }

    public Single<DeleteResult> clearAllDataForCharacter(String actorId) {
        log.info("deleting inventory data for actor: {}", actorId);
        // This is for test purposes!
        return inventoryRepository.deleteAllInventoryDataForCharacter(actorId);
    }
}
