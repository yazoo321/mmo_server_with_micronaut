package server.player.attributes.levels.types;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ClassesAttributeTypes {

    MAGE("MAGE"),
    FIGHTER("FIGHTER"),
    CLERIC("CLERIC");


    public final String type;
}