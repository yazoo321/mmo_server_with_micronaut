package server.items.types.accessories;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.model.types.CapeSlot;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.ItemInstance;
import server.items.model.Stacking;
import server.items.types.ItemType;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonTypeName("CAPE")
@EqualsAndHashCode(callSuper = false)
public class Cape extends Item {

    public Cape(
            String itemId,
            String itemName,
            Map<String, Double> itemEffects,
            Stacking stacking,
            Integer value,
            ItemConfig config) {
        super(itemId, itemName, ItemType.CAPE.getType(), itemEffects, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String actorId, ItemInstance itemInstance) {
        return new CapeSlot(actorId, itemInstance);
    }
}
