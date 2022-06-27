package server.player.character.equippable.model.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.items.types.ItemType;
import server.player.character.equippable.model.EquippedItems;

@Data
@NoArgsConstructor
@JsonTypeName("SHIELD")
@EqualsAndHashCode(callSuper=false)
public class ShieldSlot extends EquippedItems {

    public ShieldSlot(String characterName, String characterItemId) {
        super(characterName, characterItemId, ItemType.SHIELD.getType());
    }

}
