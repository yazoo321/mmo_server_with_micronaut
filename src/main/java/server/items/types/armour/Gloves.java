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
import server.items.equippable.model.types.GlovesSlot;

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
    public EquippedItems createEquippedItem(String characterName, ItemInstance itemInstance) {
        return new GlovesSlot(characterName, itemInstance);
    }

}
