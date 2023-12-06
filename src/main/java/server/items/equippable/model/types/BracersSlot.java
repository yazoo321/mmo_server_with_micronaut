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
@JsonTypeName("BRACERS")
@EqualsAndHashCode(callSuper = false)
public class BracersSlot extends EquippedItems {

    public BracersSlot(String actorId, ItemInstance itemInstance) {
        super(actorId, itemInstance, ItemType.BRACERS.getType());
    }
}
