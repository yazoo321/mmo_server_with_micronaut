package server.player.character.equippable.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericEquipData {

    String characterItemId;
    EquippedItems equippedItems;
    List<EquippedItems> equippedItemsList;

}
