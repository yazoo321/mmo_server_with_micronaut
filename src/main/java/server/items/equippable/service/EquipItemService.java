package server.items.equippable.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import server.items.types.ItemType;

import static server.attribute.stats.types.StatsTypes.*;

@Slf4j
@Singleton
public class EquipItemService {

    @Inject EquipRepository equipRepository;

    @Inject InventoryService inventoryService;

    @Inject StatsService statsService;

    public Single<EquippedItems> equipItem(String itemInstanceId, String actorId) {
        // in order to equip item, first un-equip item from slot if one exists
        return inventoryService
                .getInventory(actorId)
                .doOnError(e -> log.error("Failed to get character inventory, {}", e.getMessage()))
                .flatMap(
                        inventory -> {
                            List<CharacterItem> items = inventory.getCharacterItems();
                            CharacterItem characterItem =
                                    getCharacterItemByInstance(items, itemInstanceId);
                            ItemInstance instance = characterItem.getItemInstance();

                            String slotType = characterItem.getItemInstance().getItem().getCategory();

                            // TODO: Make this async
                            EquippedItems equippedItem =
                                    equipRepository
                                            .getCharacterItemSlot(actorId, slotType)
                                            .blockingGet();
                            if (equippedItem != null) {
                                items = getItemsAfterUnequip(equippedItem);
                                // the items object has been refreshed, need to re-sync object
                                characterItem =
                                        getCharacterItemByInstance(items, itemInstanceId);
                            }

                            equippedItem =
                                    characterItem
                                            .getItemInstance()
                                            .getItem()
                                            .createEquippedItem(actorId, instance);

                            // setting location to 'invalid' value, to not show it in inventory
                            characterItem.setLocation(new Location2D(-1, -1));
                            // TODO: Make this async
                            inventoryService.updateInventoryItems(actorId, items).blockingSubscribe();

                            return equipRepository
                                    .insert(equippedItem, actorId)
                                    .map(its -> {
                                        updateCharacterItemStats(actorId);
                                        return its;
                                    });
                        });
    }

    public Single<String> unEquipItem(String itemInstanceId, String actorId) {
        return inventoryService
                .unequipItem(itemInstanceId, actorId)
                .doOnError(e -> {throw new EquipException("Failed to un-equip item");})
                .map(itemList -> {
                    equipRepository.deleteEquippedItem(itemInstanceId).blockingSubscribe();
                    updateCharacterItemStats(actorId);

                    return itemInstanceId;
                });
    }

    private List<CharacterItem> getItemsAfterUnequip(EquippedItems item) {
        return unequipItemAndGetInventory(
                item.getItemInstance().getItemInstanceId(),
                item.getActorId())
                .blockingGet()
                .getCharacterItems();
    }

    public Single<Inventory> unequipItemAndGetInventory(String itemInstanceId, String actorId) {
        return unEquipItem(itemInstanceId, actorId)
                .flatMap(instanceId -> inventoryService.getInventory(actorId));
    }

    public Single<List<EquippedItems>> getEquippedItems(String actorId) {
        return equipRepository.getEquippedItemsForCharacter(actorId);
    }

    public Single<Map<String, EquippedItems>> getEquippedItemsMap(String actorId) {
        return equipRepository.getActorEquippedItems(actorId);
    }

    public Single<List<EquippedItems>> getEquippedItems(Set<String> actorIds) {
        return equipRepository.getEquippedItemsForCharacters(actorIds);
    }

    public static CharacterItem getCharacterItemByInstance(
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

    public void updateCharacterItemStats(String actorId) {
        getEquippedItems(actorId)
                .doOnError(e -> log.error(e.getMessage()))
                .doOnSuccess(
                        items -> {
                            addCustomEffectsToMainHandAndOffhand(items);
                            Map<String, Double> effects = new HashMap<>();
                            items.forEach(
                                    i ->
                                            Stats.mergeLeft(
                                                    effects,
                                                    i.getItemInstance()
                                                            .getItem()
                                                            .getItemEffects()));
                            statsService.updateItemStats(actorId, effects);
                        })
                .blockingSubscribe();
    }

    public void addCustomEffectsToMainHandAndOffhand(List<EquippedItems> items) {
        // we're not going to permanently add these properties
        for (EquippedItems item : items) {
            if (item.getCategory().equals(ItemType.WEAPON.getType())) {
                Map<String, Double> itemEffects = item.getItemInstance().getItem().getItemEffects();
                itemEffects.put(MAIN_HAND_ATTACK_SPEED.getType(),
                        itemEffects.get(BASE_ATTACK_SPEED.getType()));
            }

            if (item.getCategory().equals(ItemType.SHIELD.getType())) {
                Map<String, Double> itemEffects = item.getItemInstance().getItem().getItemEffects();
                itemEffects.put(OFF_HAND_ATTACK_SPEED.getType(),
                        itemEffects.get(BASE_ATTACK_SPEED.getType()));
            }
        }

    }
}
