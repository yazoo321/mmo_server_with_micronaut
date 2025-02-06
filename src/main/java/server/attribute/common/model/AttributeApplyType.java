package server.attribute.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttributeApplyType {
    DERIVED_STATS("DERIVED_STATS"),
    ON_HIT_APPLY("ON_HIT_APPLY"),
    ON_HIT_CONSUME("ON_HIT_CONSUME"),
    ON_MISS_APPLY("ON_MISS_APPLY"),
    ON_MISS_CONSUME("ON_MISS_CONSUME"),
    ON_DODGE_APPLY("ON_DODGE_APPLY"),
    ON_DODGE_CONSUME("ON_DODGE_CONSUME"),
    ON_DMG_APPLY("ON_DMG_APPLY"),
    ON_DMG_CONSUME("ON_DMG_CONSUME"),
    ON_CRIT_APPLY("ON_CRIT_APPLY"),
    ON_CRIT_CONSUME("ON_CRIT_CONSUME"),
    ON_DEATH_APPLY("ON_DEATH_APPLY"),
    ON_DEATH_CONSUME("ON_DEATH_CONSUME");
    public final String type;
}
