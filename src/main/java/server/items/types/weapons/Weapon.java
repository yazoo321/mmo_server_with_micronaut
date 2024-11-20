package server.items.types.weapons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.model.types.WeaponSlot1;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.ItemInstance;
import server.items.model.Stacking;
import server.items.types.ItemType;

@Data
@Serdeable
@NoArgsConstructor
@JsonTypeName("WEAPON")
@EqualsAndHashCode(callSuper = false)
public class Weapon extends Item {

    public Weapon(
            String itemId,
            String itemName,
            Map<String, Double> itemEffects,
            Map<String, Integer> requirements,
            Integer quality,
            Stacking stacking,
            Integer value,
            ItemConfig config) {
        super(itemId, itemName, ItemType.WEAPON.getType(), itemEffects, requirements, quality, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String actorId, ItemInstance itemInstance) {
        return new WeaponSlot1(actorId, itemInstance);
    }
}
