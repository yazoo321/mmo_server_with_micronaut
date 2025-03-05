package server.attribute.status.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusTypes {
    //    Base statuses
    UNCONSCIOUS("UNCONSCIOUS"),
    DEAD("DEAD"),
    STUNNED("STUNNED"),
    SILENCED("SILENCED"),

    //   player affects
    CANNOT_ACT("CANNOT_ACT"),
    CANNOT_MOVE("CANNOT_MOVE"),
    CANNOT_CAST("CANNOT_CAST"),
    CANNOT_ATTACK("CANNOT_ATTACK"),
    CANNOT_HEAL("CANNOT_HEAL"),

    //    Damaging statuses
    BLEEDING("BLEEDING"),
    BURNING("BURNING"),
    FROSTED("FROSTED"),


    // Statuses which affect stats, have to be in line with StatsTypes
    ATTRIBUTE_MOD("ATTRIBUTE_MOD"),
    MOVE_SPEED("MOVE_SPEED"),
    PHY_AMP("PHY_AMP"),

    ARMOR_MOD("ARMOR_MOD");

    public final String type;
}
