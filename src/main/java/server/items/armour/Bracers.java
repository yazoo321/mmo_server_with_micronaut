package server.items.armour;

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
import server.player.character.equippable.model.EquippedItems;
import server.player.character.equippable.model.types.BracersSlot;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("BRACERS")
@EqualsAndHashCode(callSuper=false)
public class Bracers extends Item {

    public Bracers(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
        super(itemId, itemName, ItemType.BRACERS.getType(), tags, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String characterName, ItemInstance itemInstance) {
        return new BracersSlot(characterName, itemInstance);
    }

}
