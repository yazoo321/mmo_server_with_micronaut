package server.items.types;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ItemType {
    // weapons
    WEAPON("WEAPON"),
    SHIELD("SHIELD"),

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
    RING("RING"),
    NECK("NECK"),

    // consumables
    CONSUMABLE("CONSUMABLE");

    public final String type;

}