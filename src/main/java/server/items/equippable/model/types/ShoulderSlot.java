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
@JsonTypeName("SHOULDER")
@EqualsAndHashCode(callSuper=false)
public class ShoulderSlot extends EquippedItems {

    public ShoulderSlot(String characterName, ItemInstance itemInstance) {
        super(characterName, itemInstance, ItemType.SHOULDER.getType());
    }
}
