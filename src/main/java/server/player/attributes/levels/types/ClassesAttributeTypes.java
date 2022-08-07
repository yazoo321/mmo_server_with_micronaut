package server.player.attributes.levels.types;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ClassesAttributeTypes {

    MAGE("mage"),
    FIGHTER("fighter"),
    CLERIC("cleric");


    public final String type;
}