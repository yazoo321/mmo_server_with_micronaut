package server.player.character.inventory.model;


import lombok.Data;
import server.common.dto.Location;
import server.items.dropped.model.DroppedItem;
import server.items.dto.Item;

@Data
public class GenericInventoryData {

    Inventory inventory;
    Item item;
    String characterName;
    CharacterItem characterItem;
    DroppedItem droppedItem;
    Location location;

}
