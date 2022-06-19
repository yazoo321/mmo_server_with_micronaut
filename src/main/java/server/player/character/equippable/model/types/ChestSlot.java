package server.player.character.equippable.model.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import server.player.character.equippable.SlotType;
import server.player.character.equippable.model.EquippableSlots;

@Data
@JsonTypeName("HELM")
@EqualsAndHashCode(callSuper=false)
public class ChestSlot extends EquippableSlots {

    public ChestSlot(String characterName) {
        super(characterName, SlotType.CHEST.getType());
    }
}
