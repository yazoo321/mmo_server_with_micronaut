package server.items.types.weapons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.common.dto.Tag;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.ItemInstance;
import server.items.model.Stacking;
import server.items.types.ItemType;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.model.types.WeaponSlot1;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("WEAPON")
@EqualsAndHashCode(callSuper=false)
public class Weapon extends Item {

    public Weapon(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
        super(itemId, itemName, ItemType.WEAPON.getType(), tags, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String characterName, ItemInstance itemInstance) {
        return new WeaponSlot1(characterName, itemInstance);
    }

}
