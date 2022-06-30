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
import server.player.character.equippable.model.types.ShoulderSlot;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("SHOULDER")
@EqualsAndHashCode(callSuper=false)
public class Shoulder extends Item {

    public Shoulder(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
        super(itemId, itemName, ItemType.SHOULDER.getType(), tags, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String characterName, String itemInstanceId) {
        return new ShoulderSlot(characterName, itemInstanceId);
    }

}
