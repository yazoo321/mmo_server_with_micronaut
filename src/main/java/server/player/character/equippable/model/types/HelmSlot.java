package server.player.character.equippable.model.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.player.character.equippable.SlotType;
import server.player.character.equippable.model.EquippableSlots;

@Data
@NoArgsConstructor
@JsonTypeName("CHEST")
@EqualsAndHashCode(callSuper=false)
public class HelmSlot extends EquippableSlots {

    public HelmSlot(String characterName) {
        super(characterName, SlotType.HELM.getType());
    }

}
