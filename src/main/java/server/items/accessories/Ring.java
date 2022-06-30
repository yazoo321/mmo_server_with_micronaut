package server.items.accessories;

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
import server.player.character.equippable.model.types.RingSlot1;
import server.player.character.equippable.model.types.ShoulderSlot;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("RING")
@EqualsAndHashCode(callSuper=false)
public class Ring extends Item {

    public Ring(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
        super(itemId, itemName, ItemType.RING.getType(), tags, stacking, value, config);
    }

    @Override
    public EquippedItems createEquippedItem(String characterName, String itemInstanceId) {
        return new RingSlot1(characterName, itemInstanceId);
    }

}
