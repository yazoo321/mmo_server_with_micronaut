package server.player.character.equippable.model.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.items.types.ItemType;
import server.player.character.equippable.model.EquippedItems;

@Data
@NoArgsConstructor
@JsonTypeName("CHEST")
@EqualsAndHashCode(callSuper=false)
public class HelmSlot extends EquippedItems {

    public HelmSlot(String characterName, String itemInstanceId) {
        super(characterName, itemInstanceId, ItemType.HELM.getType());
    }

}
