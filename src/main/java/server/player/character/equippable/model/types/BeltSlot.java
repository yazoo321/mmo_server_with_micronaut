package server.player.character.equippable.model.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.items.model.ItemInstance;
import server.items.types.ItemType;
import server.player.character.equippable.model.EquippedItems;

@Data
@NoArgsConstructor
@JsonTypeName("BELT")
@EqualsAndHashCode(callSuper=false)
public class BeltSlot extends EquippedItems {

    public BeltSlot(String characterName, ItemInstance itemInstance) {
        super(characterName, itemInstance, ItemType.BELT.getType());
    }
}
