package server.items.equippable.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location2D;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.model.exceptions.EquipException;
import server.items.equippable.repository.EquipRepository;
import server.items.inventory.model.CharacterItem;
import server.items.inventory.model.Inventory;
import server.items.inventory.model.exceptions.InventoryException;
import server.items.inventory.service.InventoryService;
import server.items.model.ItemInstance;

@Slf4j
@Singleton
public class EquipItemService {

    @Inject EquipRepository equipRepository;

    @Inject InventoryService inventoryService;

    public Single<EquippedItems> equipItem(String itemInstanceId, String characterName) {
        // in order to equip item, first un-equip item from slot if one exists
        return inventoryService
                .getInventory(characterName)
                .doOnError(e -> log.error("Failed to get character inventory, {}", e.getMessage()))
                .flatMap(
                        inventory -> {
                            List<CharacterItem> items = inventory.getCharacterItems();
                            ItemInstance instance =
                                    getCharacterItemByInstance(items, itemInstanceId)
                                            .getItemInstance();

                            String slotType = instance.getItem().getCategory();

                            // TODO: Make this async
                            EquippedItems equippedItem =
                                    equipRepository
                                            .getCharacterItemSlot(characterName, slotType)
                                            .blockingGet();
                            if (equippedItem != null) {
                                // TODO: Make this async
                                items =
                                        unequipItem(
                                                        equippedItem
                                                                .getItemInstance()
                                                                .getItemInstanceId(),
                                                        characterName)
                                                .blockingGet()
                                                .getCharacterItems();
                            }

                            // the items object has been refreshed, need to re-sync object
                            CharacterItem characterItem =
                                    getCharacterItemByInstance(items, itemInstanceId);

                            equippedItem =
                                    characterItem
                                            .getItemInstance()
                                            .getItem()
                                            .createEquippedItem(characterName, instance);

                            // setting location to 'invalid' value, to not show it in inventory
                            characterItem.setLocation(new Location2D(-1, -1));
                            // TODO: Make this async
                            inventoryService
                                    .updateInventoryItems(characterName, items)
                                    .blockingGet();

                            return equipRepository.insert(equippedItem);
                        });
    }

    public Single<Inventory> unequipItem(String itemInstanceId, String characterName) {
        return inventoryService
                .unequipItem(itemInstanceId, characterName)
                .doOnError(
                        e -> {
                            log.warn("Failed to unequip item, {}", e.getMessage());
                            throw new EquipException("Failed to unequip item");
                        })
                .flatMap(
                        itemList -> {
                            // TODO: Make async
                            equipRepository.deleteEquippedItem(itemInstanceId).blockingGet();
                            return inventoryService.getInventory(characterName);
                        });
        //                .flatMap(itemList ->
        //                        equipRepository.deleteEquippedItem(itemInstanceId)
        //                                .doOnError(e -> {
        //                                    log.warn("Failed to unequip item, {}",
        // e.getMessage());
        //                                    throw new EquipException("Failed to delete equipped
        // item");
        //                                })
        //                                .flatMap(res ->
        // inventoryService.getInventory(characterName)))
        //                                .subscribe();
    }

    public Single<List<EquippedItems>> getEquippedItems(String characterName) {
        return equipRepository.getEquippedItemsForCharacter(characterName);
    }

    private CharacterItem getCharacterItemByInstance(
            List<CharacterItem> items, String itemInstanceId) {
        return items.stream()
                .filter(
                        iterator ->
                                iterator.getItemInstance()
                                        .getItemInstanceId()
                                        .equals(itemInstanceId))
                .findFirst()
                .orElseThrow(
                        () -> new InventoryException("The item trying to equip does not exist"));
    }
}
