package server.items.weapons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.common.dto.Tag;
import server.items.dto.Item;
import server.items.dto.Stacking;
import server.items.types.ItemType;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("weapon")
@EqualsAndHashCode(callSuper=false)
public class Weapon extends Item {

    public Weapon(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value) {
        super(itemId, itemName, ItemType.WEAPON.getType(), tags, stacking, value);
    }

}
