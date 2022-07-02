package server.player.character.equippable.controller;

import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import server.player.character.equippable.model.EquippedItems;
import server.player.character.equippable.model.GenericEquipData;
import server.player.character.equippable.service.EquipItemService;

import javax.inject.Inject;
import java.util.List;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/v1/equipped")
public class EquipItemsController {

    @Inject
    EquipItemService equipItemService;


    @Post("/equip")
    public GenericEquipData equip(@Body GenericEquipData equipData, @Header String characterName) {
        EquippedItems equippedItems = equipItemService.equipItem(equipData.getItemInstanceId(), characterName);

        return GenericEquipData.builder().equippedItems(equippedItems).build();
    }

    @Post("/unequip")
    public void unequip(@Body GenericEquipData equipData, @Header String characterName) {
        equipItemService.unequipItem(equipData.getItemInstanceId(), characterName);
    }

    @Get()
    public GenericEquipData getCharacterEquippedItems(@Header String characterName) {
        List<EquippedItems> equippedItemsList = equipItemService.getEquippedItems(characterName);
        return GenericEquipData.builder().equippedItemsList(equippedItemsList).build();
    }
}
