package server.items.inventory.model.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.model.GenericEquipData;
import server.items.inventory.model.Inventory;
import server.items.model.Item;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericInventoryData {

    List<Item> items;
    String itemInstanceId;
    String itemId;
    String characterName;
    Location2D itemInventoryLocation;
    String droppedItemId;
    Location location;
    Inventory inventory;

    GenericEquipData equipData;
    EquippedItems equippedItems;
    List<EquippedItems> equippedItemsList;
}
