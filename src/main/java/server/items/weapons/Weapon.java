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
import server.player.character.equippable.SlotType;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("WEAPON")
@EqualsAndHashCode(callSuper=false)
public class Weapon extends Item {

    public Weapon(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
        super(itemId, itemName, ItemType.WEAPON.getType(), tags, stacking, value, config);
    }

    public List<SlotType> getValidSlotTypes() {
        return List.of(SlotType.WEAPON1, SlotType.WEAPON2);
    }
}
