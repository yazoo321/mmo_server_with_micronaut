package server.attribute.stats.model.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClassTypes {
    MAGE("mage"),
    FIGHTER("fighter"),
    CLERIC("cleric");

    public final String type;
}
