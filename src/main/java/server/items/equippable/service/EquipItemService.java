package server.items.equippable.service;

import com.mongodb.client.result.DeleteResult;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static server.attribute.stats.types.StatsTypes.*;

@Slf4j
@Singleton
public class EquipItemService {

    @Inject EquipRepository equipRepository;

    @Inject InventoryService inventoryService;

    @Inject StatsService statsService;

    public Single<EquippedItems> equipItem(String itemInstanceId, String actorId) {
        return inventoryService
                .getInventory(actorId)
                .doOnError(e -> log.error("Failed to get character inventory: {}", e.getMessage()))
                .flatMap(
                        inventory -> {
                            List<CharacterItem> items = inventory.getCharacterItems();
                            CharacterItem characterItem =
                                    getCharacterItemByInstance(items, itemInstanceId);
                            ItemInstance instance = characterItem.getItemInstance();
                            String slotType = instance.getItem().getCategory();

                            // Check if an item is already equipped in the same slot
                            return equipRepository
                                    .getCharacterItemSlot(actorId, slotType)
                                    .flatMapSingle(
                                            existingEquippedItem -> {
                                                // If an item is already equipped, unequip it
                                                return unEquipItemAndGetCharacterItems(
                                                                existingEquippedItem
                                                                        .getItemInstance()
                                                                        .getItemInstanceId(),
                                                                actorId)
                                                        .flatMap(
                                                                updatedCharacterItems ->
                                                                        updateAndEquipItem(
                                                                                actorId,
                                                                                updatedCharacterItems,
                                                                                itemInstanceId));
                                            })
                                    .switchIfEmpty(
                                            Single.defer(
                                                    () ->
                                                            updateAndEquipItem(
                                                                    actorId,
                                                                    items,
                                                                    itemInstanceId)));
                        })
                .onErrorResumeNext(
                        e -> {
                            log.error("Error during item equip: {}", e.getMessage());
                            return Single.error(new EquipException("Failed to equip item"));
                        });
    }

    private Single<EquippedItems> updateAndEquipItem(
            String actorId, List<CharacterItem> items, String itemInstanceId) {
        CharacterItem characterItem = getCharacterItemByInstance(items, itemInstanceId);
        ItemInstance instance = characterItem.getItemInstance();
        // Set location to 'invalid' to remove from inventory display
        characterItem.setLocation(new Location2D(-1, -1));

        // Update inventory with the new state
        return inventoryService
                .updateInventoryItems(actorId, items)
                .flatMap(
                        updated -> {
                            EquippedItems equippedItem =
                                    instance.getItem().createEquippedItem(actorId, instance);
                            return equipRepository
                                    .insert(equippedItem, actorId)
                                    .doOnSuccess(its -> updateCharacterItemStats(actorId));
                        });
    }

    public Single<List<CharacterItem>> unEquipItemAndGetCharacterItems(
            String itemInstanceId, String actorId) {
        return inventoryService
                .unequipItem(itemInstanceId, actorId)
                .doOnError(
                        e -> {
                            throw new EquipException("Failed to un-equip item");
                        })
                .map(
                        itemList -> {
                            equipRepository
                                    .deleteEquippedItem(actorId, itemInstanceId)
                                    .blockingSubscribe();
                            updateCharacterItemStats(actorId);

                            return itemList;
                        });
    }

    public Single<String> unEquipItem(String itemInstanceId, String actorId) {
        return inventoryService
                .unequipItem(itemInstanceId, actorId)
                .doOnError(
                        e -> {
                            throw new EquipException("Failed to un-equip item");
                        })
                .map(
                        itemList -> {
                            equipRepository
                                    .deleteEquippedItem(actorId, itemInstanceId)
                                    .blockingSubscribe();
                            updateCharacterItemStats(actorId);

                            return itemInstanceId;
                        });
    }

    private List<CharacterItem> getItemsAfterUnequip(EquippedItems item) {
        return unequipItemAndGetInventory(
                        item.getItemInstance().getItemInstanceId(), item.getActorId())
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
                itemEffects.put(
                        MAIN_HAND_ATTACK_SPEED.getType(),
                        itemEffects.get(BASE_ATTACK_SPEED.getType()));
                itemEffects.put(MAINHAND_WEAPON_DAMAGE.getType(),
                        itemEffects.get(WEAPON_DAMAGE.getType()));
                itemEffects.put(MAINHAND_ATTACK_DISTANCE.getType(),
                        itemEffects.get(ATTACK_DISTANCE.getType()));
            }

            if (item.getCategory().equals(ItemType.SHIELD.getType())) {
                Map<String, Double> itemEffects = item.getItemInstance().getItem().getItemEffects();
                itemEffects.put(
                        OFF_HAND_ATTACK_SPEED.getType(),
                        itemEffects.get(BASE_ATTACK_SPEED.getType()));
                itemEffects.put(
                        OFFHAND_WEAPON_DAMAGE.getType(),
                        itemEffects.get(WEAPON_DAMAGE.getType()));
                itemEffects.put(OFFHAND_ATTACK_DISTANCE.getType(),
                        itemEffects.get(ATTACK_DISTANCE.getType()));
            }
        }
    }

    public Single<DeleteResult> deleteCharacterEquippedItems(String actorId) {
        // used by delete character
        return equipRepository.deleteActorEquippedItems(actorId);
    }
}
