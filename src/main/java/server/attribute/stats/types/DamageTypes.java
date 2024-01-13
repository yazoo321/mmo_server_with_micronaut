package server.attribute.stats.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DamageTypes {
    SLASHING("SLASHING"),
    PIERCING("PIERCING"),
    BLUDGEONING("BLUDGEONING"),
    PHYSICAL("PHYSICAL"),
    MAGIC("MAGIC"),
    FIRE("FIRE"),
    COLD("COLD"),
    LIGHTNING("LIGHTNING"),

    POSITIVE("POSITIVE");

    public final String type;
}
