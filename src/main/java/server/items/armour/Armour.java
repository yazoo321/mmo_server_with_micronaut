package server.items.armour;

import server.common.dto.Tag;
import server.items.dto.Item;
import server.items.types.ItemType;

import java.util.List;

public class Armour extends Item {

    public Armour(String itemId, String itemName, List<Tag> tags, Integer value) {
        super(itemId, itemName, ItemType.ARMOUR.getType(), tags, value);
    }

    // use tags to get specific dynamic data


}
