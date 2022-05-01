package server.items.weapons;

import server.common.dto.Tag;
import server.items.dto.Item;
import server.items.types.ItemType;

import java.util.List;

public class Weapon extends Item {
    public Weapon(String itemId, String itemName, List<Tag> tags) {
        super(itemId, itemName, ItemType.WEAPON.getType(), tags);
    }
}
