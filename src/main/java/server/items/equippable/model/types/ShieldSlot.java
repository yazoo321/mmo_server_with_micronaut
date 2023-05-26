package server.items.equippable.model.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.items.model.ItemInstance;
import server.items.types.ItemType;
import server.items.equippable.model.EquippedItems;

@Data
@NoArgsConstructor
@JsonTypeName("SHIELD")
@EqualsAndHashCode(callSuper=false)
public class ShieldSlot extends EquippedItems {

    public ShieldSlot(String characterName, ItemInstance itemInstance) {
        super(characterName, itemInstance, ItemType.SHIELD.getType());
    }

}
