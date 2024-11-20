package server.items.types.armour;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.model.types.ShoulderSlot;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.ItemInstance;
import server.items.model.Stacking;
import server.items.types.ItemType;

@Data
@NoArgsConstructor
@JsonTypeName("SHOULDER")
@EqualsAndHashCode(callSuper = false)
public class Shoulder extends Item {

    public Shoulder(
            String itemId,
            String itemName,
            Map<String, Double> itemEffects,
            Map<String, Integer> requirements,
            Integer quality,
            Stacking stacking,
            Integer value,
            ItemConfig config) {
        super(itemId, itemName, ItemType.SHOULDER.getType(), itemEffects, requirements, quality,
                stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String actorId, ItemInstance itemInstance) {
        return new ShoulderSlot(actorId, itemInstance);
    }
}
