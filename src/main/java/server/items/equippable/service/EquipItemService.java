package server.items.equippable.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
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

    @Inject StatsService statsService;

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
                                        unequipItemAndGetInventory(
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

                            return equipRepository
                                    .insert(equippedItem)
                                    .map(
                                            equippedItems -> {
                                                updateCharacterItemStats(characterName);

                                                return equippedItems;
                                            });
                        });
    }

    public Single<String> unEquipItem(String itemInstanceId, String characterName) {
        return inventoryService
                .unequipItem(itemInstanceId, characterName)
                .doOnError(
                        e -> {
                            log.warn("Failed to unequip item, {}", e.getMessage());
                            throw new EquipException("Failed to unequip item");
                        })
                .map(
                        itemList -> {
                            // TODO: Make async
                            equipRepository.deleteEquippedItem(itemInstanceId).blockingGet();
                            updateCharacterItemStats(characterName);

                            return itemInstanceId;
                        });
    }

    public Single<Inventory> unequipItemAndGetInventory(
            String itemInstanceId, String characterName) {
        return unEquipItem(itemInstanceId, characterName)
                .flatMap(instanceId -> inventoryService.getInventory(characterName));
    }

    public Single<List<EquippedItems>> getEquippedItems(String characterName) {
        return equipRepository.getEquippedItemsForCharacter(characterName);
    }

    public Single<List<EquippedItems>> getEquippedItems(Set<String> characterNames) {
        return equipRepository.getEquippedItemsForCharacters(characterNames);
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

    public void updateCharacterItemStats(String playerName) {
        getEquippedItems(playerName)
                .doOnError(
                        e -> log.error("Failed to update character item stats, {}", e.getMessage()))
                .doOnSuccess(
                        items -> {
                            Map<String, Double> effects = new HashMap<>();
                            items.forEach(
                                    i ->
                                            Stats.mergeLeft(
                                                    effects,
                                                    i.getItemInstance()
                                                            .getItem()
                                                            .getItemEffects()));
                            statsService.updateItemStats(playerName, effects);
                        })
                .subscribe();
    }
}
