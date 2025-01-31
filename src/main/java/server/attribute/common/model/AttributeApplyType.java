package server.attribute.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttributeApplyType {

    ON_HIT_APPLY("ON_HIT_APPLY"),
    ON_HIT_CONSUME("ON_HIT_CONSUME"),
    ON_MISS_APPLY("ON_MISS_APPLY"),
    ON_MISS_CONSUME("ON_MISS_CONSUME"),
    ON_DMG_APPLY("ON_DMG_APPLY"),
    ON_DMG_CONSUME("ON_DMG_CONSUME");
    public final String type;
}
