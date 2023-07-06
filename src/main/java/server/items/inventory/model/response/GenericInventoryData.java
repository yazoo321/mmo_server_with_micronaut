package server.items.inventory.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.items.equippable.model.EquippedItems;
import server.items.inventory.model.Inventory;
import server.items.model.DroppedItem;
import server.items.model.Item;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericInventoryData {

    List<Item> items;
    String itemInstanceId;
    String itemId;
    String characterName;
    Location location;
    Inventory inventory;

    List<EquippedItems> equippedItems;
    List<DroppedItem> droppedItems;
}
