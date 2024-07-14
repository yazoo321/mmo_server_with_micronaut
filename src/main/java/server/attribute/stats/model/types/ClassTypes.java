package server.attribute.stats.model.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClassTypes {
    MAGE("mage"),
    FIGHTER("fighter"),
    CLERIC("cleric"),
    RANGER("ranger");

    public final String type;
}
