package server.items.types.armour;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.model.types.BootsSlot;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.ItemInstance;
import server.items.model.Stacking;
import server.items.types.ItemType;

@Data
@NoArgsConstructor
@JsonTypeName("BOOTS")
@EqualsAndHashCode(callSuper = false)
public class Boots extends Item {

    public Boots(
            String itemId,
            String itemName,
            Map<String, Double> itemEffects,
            Map<String, Integer> requirements,
            Integer quality,
            Stacking stacking,
            Integer value,
            ItemConfig config) {
        super(itemId, itemName, ItemType.BOOTS.getType(), itemEffects, requirements, quality, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String actorId, ItemInstance itemInstance) {
        return new BootsSlot(actorId, itemInstance);
    }
}
