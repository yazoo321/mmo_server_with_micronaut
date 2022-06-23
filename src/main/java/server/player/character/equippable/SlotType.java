package server.player.character.equippable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SlotType {
    WEAPON1("weapon"),
    WEAPON2("WEAPON2"),
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
    NECK("NECK");

    public final String type;

}
