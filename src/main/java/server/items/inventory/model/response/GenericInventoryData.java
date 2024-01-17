package server.items.inventory.model.response;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location;
import server.items.equippable.model.EquippedItems;
import server.items.inventory.model.Inventory;
import server.items.model.DroppedItem;
import server.items.model.Item;

import java.util.List;

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
}
