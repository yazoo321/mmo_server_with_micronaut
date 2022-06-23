package server.player.character.equippable.model.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.player.character.equippable.SlotType;
import server.player.character.equippable.model.EquippedItems;



//
//    public Weapon(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
//        super(itemId, itemName, ItemType.WEAPON.getType(), tags, stacking, value, config);
//    }

@Data
@NoArgsConstructor
@JsonTypeName("WEAPON1")
@EqualsAndHashCode(callSuper=false)
public class WeaponSlot1 extends EquippedItems {

    public WeaponSlot1(String characterName, String characterItemId) {
        super(characterName, characterItemId, SlotType.WEAPON1.getType());
    }

}
