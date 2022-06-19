package server.items.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

//@Getter
//public enum ItemType {
//    ARMOUR("ARMOUR"),
//    WEAPON("WEAPON"),
//    CONSUMABLE("CONSUMABLE");
//
//    public final String type;
//
//    private ItemType(String type) {
//        this.type = type;
//    }
//
//}

@Getter
@AllArgsConstructor
public enum ItemType {
    // weapons
    WEAPON("WEAPON"),


    // armour
    HELM("HELM"),
    CHEST("CHEST"),
    BELT("BELT"),
    LEGS("LEGS"),
    BOOTS("BOOTS"),
    CAPE("CAPE"),
    SHOULDER("SHOULDER"),
    BRACERS("BRACERS"),
    GLOVES("GLOVES"),
    SHIRT("SHIRT"),
    RING1("RING1"),
    RING2("RING2"),
    NECK("NECK"),

    // consumables
    CONSUMABLE("CONSUMABLE");

    public final String type;

}