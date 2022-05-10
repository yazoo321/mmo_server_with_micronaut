package server.player.character.inventory.model;


import lombok.Data;
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.items.dto.Item;

@Data
public class GenericInventoryData {

    Item item;
    String itemId;
    String characterName;
    Location2D itemInventoryLocation;
    String droppedItemId;
    Location location;

}
