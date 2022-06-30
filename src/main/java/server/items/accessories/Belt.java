package server.items.accessories;

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
import server.player.character.equippable.model.types.ChestSlot;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("BELT")
@EqualsAndHashCode(callSuper=false)
public class Belt extends Item {

    public Belt(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
        super(itemId, itemName, ItemType.BELT.getType(), tags, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String characterName, String itemInstanceId) {
        return new BeltSlot(characterName, itemInstanceId);
    }

}
