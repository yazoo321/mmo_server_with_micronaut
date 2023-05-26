package server.items.inventory.model.response;


import lombok.Data;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.items.model.Item;

import java.util.List;

@Data
public class GenericInventoryData {

    List<Item> items;
    String itemId;
    String characterName;
    Location2D itemInventoryLocation;
    String droppedItemId;
    Location location;

}
