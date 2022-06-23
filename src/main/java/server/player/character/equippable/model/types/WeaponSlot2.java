package server.player.character.equippable.model.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.player.character.equippable.SlotType;
import server.player.character.equippable.model.EquippedItems;

@Data
@NoArgsConstructor
@JsonTypeName("WEAPON2")
@EqualsAndHashCode(callSuper=false)
public class WeaponSlot2 extends EquippedItems {

    public WeaponSlot2(String characterName, String characterItemId) {
        super(characterName, characterItemId, SlotType.WEAPON2.getType());
    }

}
