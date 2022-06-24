package server.player.character.equippable.model.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import server.items.types.ItemType;
import server.player.character.equippable.model.EquippedItems;

@Data
@JsonTypeName("BELT")
@EqualsAndHashCode(callSuper=false)
public class BeltSlot extends EquippedItems {

    public BeltSlot(String characterName, String characterItemId) {
        super(characterName, characterItemId, ItemType.BELT.getType());
    }
}
