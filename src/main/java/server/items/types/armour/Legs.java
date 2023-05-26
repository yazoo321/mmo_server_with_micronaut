package server.items.types.armour;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.common.dto.Tag;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.ItemInstance;
import server.items.model.Stacking;
import server.items.types.ItemType;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.model.types.LegsSlot;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("LEGS")
@EqualsAndHashCode(callSuper=false)
public class Legs extends Item {

    public Legs(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
        super(itemId, itemName, ItemType.LEGS.getType(), tags, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String characterName, ItemInstance itemInstance) {
        return new LegsSlot(characterName, itemInstance);
    }

}
