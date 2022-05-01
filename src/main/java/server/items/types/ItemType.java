package server.items.types;

import lombok.Getter;

@Getter
public enum ItemType {
    ARMOUR("ARMOUR"),
    WEAPON("WEAPON"),
    CONSUMABLE("CONSUMABLE");

    public final String type;

    private ItemType(String type) {
        this.type = type;
    }

}
