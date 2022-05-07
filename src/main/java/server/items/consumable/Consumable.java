package server.items.consumable;

import server.common.dto.Tag;
import server.items.dto.Item;
import server.items.types.ItemType;

import java.util.List;

public class Consumable extends Item {

    public Consumable(String itemId, String itemName, List<Tag> tags, Integer value) {
        super(itemId, itemName, ItemType.CONSUMABLE.getType(), tags, value);
    }

}
