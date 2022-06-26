package server.items.weapons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.common.dto.Tag;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.Stacking;
import server.items.types.ItemType;
import server.player.character.equippable.model.EquippedItems;
import server.player.character.equippable.model.types.ShieldSlot;
import server.player.character.equippable.model.types.WeaponSlot1;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("SHIELD")
@EqualsAndHashCode(callSuper=false)
public class Shield extends Item {

    public Shield(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
        super(itemId, itemName, ItemType.SHIELD.getType(), tags, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String characterName, String characterItemId) {
        return new ShieldSlot(characterName, characterItemId);
    }

}
