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
@JsonTypeName("RING")
@EqualsAndHashCode(callSuper = false)
public class RingSlot1 extends EquippedItems {

    public RingSlot1(String characterName, ItemInstance itemInstance) {
        super(characterName, itemInstance, ItemType.RING.getType());
    }
}
