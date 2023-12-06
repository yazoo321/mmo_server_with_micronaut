package server.items.equippable.model.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.items.equippable.model.EquippedItems;
import server.items.model.ItemInstance;
import server.items.types.ItemType;

@Data
@NoArgsConstructor
@JsonTypeName("WEAPON")
@EqualsAndHashCode(callSuper = false)
public class WeaponSlot1 extends EquippedItems {

    public WeaponSlot1(String actorId, ItemInstance itemInstance) {
        super(actorId, itemInstance, ItemType.WEAPON.getType());
    }
}
