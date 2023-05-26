package server.items.equippable.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.items.inventory.model.Inventory;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericEquipData {

    String itemInstanceId;
    EquippedItems equippedItems;
    List<EquippedItems> equippedItemsList;

    Inventory inventory;

}
