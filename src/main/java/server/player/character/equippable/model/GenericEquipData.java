package server.player.character.equippable.model;

import lombok.Builder;
import lombok.Data;
import server.player.character.inventory.model.CharacterItem;

import java.util.List;

@Data
@Builder
public class GenericEquipData {

    CharacterItem characterItem;
    EquippedItems equippedItems;
    List<EquippedItems> equippedItemsList;

}
