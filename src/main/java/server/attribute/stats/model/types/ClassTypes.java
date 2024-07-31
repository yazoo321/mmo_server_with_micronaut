package server.attribute.stats.model.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClassTypes {
    MAGE("MAGE"),
    FIGHTER("FIGHTER"),
    CLERIC("CLERIC"),
    RANGER("RANGER");

    public final String type;
}
