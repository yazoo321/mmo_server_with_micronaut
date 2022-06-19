package server.items.armour;

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
import server.player.character.equippable.model.EquippableSlots;
import server.player.character.equippable.model.types.ChestSlot;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("CHEST")
@EqualsAndHashCode(callSuper=false)
public class Chest extends Item {

    public Chest(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
        super(itemId, itemName, ItemType.CHEST.getType(), tags, stacking, value, config);
    }

    public List<SlotType> getValidSlotTypes() {
        return List.of(SlotType.CHEST);
    }

    public EquippableSlots createEquippableSlot(String characterName) {
        return new ChestSlot(characterName);
    }

}
