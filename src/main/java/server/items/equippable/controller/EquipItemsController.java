package server.items.equippable.controller;

import io.micronaut.http.annotation.*;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.items.equippable.model.GenericEquipData;
import server.items.equippable.service.EquipItemService;

@Slf4j
@Deprecated // Use websocket communication instead
@Controller("/v1/equipped")
public class EquipItemsController {

    @Inject EquipItemService equipItemService;

    @Post("/equip")
    public Single<GenericEquipData> equip(
            @Body GenericEquipData equipData, @Header String characterName) {
        return equipItemService
                .equipItem(equipData.getItemInstanceId(), characterName)
                .doOnError(e -> log.error("Failed to equip item, {}", e.getMessage()))
                .map(
                        equippedItems ->
                                GenericEquipData.builder().equippedItems(equippedItems).build());
    }

    @Post("/unequip")
    public Single<GenericEquipData> unequip(
            @Body GenericEquipData equipData, @Header String characterName) {

        return equipItemService
                .unequipItemAndGetInventory(equipData.getItemInstanceId(), characterName)
                .doOnError(e -> log.error("Failed to unequip item, {}", e.getMessage()))
                .map(i -> GenericEquipData.builder().inventory(i).build());
    }

    @Get
    public Single<GenericEquipData> getCharacterEquippedItems(@Header String characterName) {
        return equipItemService
                .getEquippedItems(characterName)
                .doOnError(e -> log.error("Failed to get equipped items, {}", e.getMessage()))
                .map(items -> GenericEquipData.builder().equippedItemsList(items).build());
    }
}
