package server.items.types.weapons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.model.types.ShieldSlot;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.ItemInstance;
import server.items.model.Stacking;
import server.items.types.ItemType;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonTypeName("SHIELD")
@EqualsAndHashCode(callSuper = false)
public class Shield extends Item {

    public Shield(
            String itemId,
            String itemName,
            Map<String, Double> itemEffects,
            Stacking stacking,
            Integer value,
            ItemConfig config) {
        super(itemId, itemName, ItemType.SHIELD.getType(), itemEffects, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String actorId, ItemInstance itemInstance) {
        return new ShieldSlot(actorId, itemInstance);
    }
}
