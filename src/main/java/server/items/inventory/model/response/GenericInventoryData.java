package server.items.inventory.model.response;

import io.micronaut.serde.annotation.Serdeable;
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
@Serdeable
public class GenericInventoryData {

    List<Item> items;
    String itemInstanceId;
    String itemId;
    String actorId;
    Location location;
    Inventory inventory;

    List<EquippedItems> equippedItems;
    List<DroppedItem> droppedItems;

    List<String> itemInstanceIds;

    // move item
    // use item instance ID to locate source item
    Location2D to;
    String category;
}
