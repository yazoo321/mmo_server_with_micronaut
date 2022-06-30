package server.items.armour;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.common.dto.Tag;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.Stacking;
import server.items.types.ItemType;
import server.player.character.equippable.model.EquippedItems;
import server.player.character.equippable.model.types.BeltSlot;
import server.player.character.equippable.model.types.GlovesSlot;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("GLOVES")
@EqualsAndHashCode(callSuper=false)
public class Gloves extends Item {

    public Gloves(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
        super(itemId, itemName, ItemType.GLOVES.getType(), tags, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String characterName, String itemInstanceId) {
        return new GlovesSlot(characterName, itemInstanceId);
    }

}
