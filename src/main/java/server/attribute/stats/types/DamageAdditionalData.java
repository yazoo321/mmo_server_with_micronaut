package server.attribute.stats.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DamageAdditionalData {
    DODGE("DODGE"),
    BLOCK("BLOCK"),
    PARRY("PARRY"),
    CRIT("CRIT"),
    HIT("HIT");

    String type;
}
