package server.common.attributes.types;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum AttributeTypes {
    // BASE
    STR("STR"),
    DEX("DEX"),
    INT("INT"),
    STA("STA"),

    HP("HP"),
    MP("MP"),

    PHY_AMP("PHY_AMP"),
    MAG_AMP("MAG_AMP"),

    DEF("DEF"),
    MAG_DEF("MAG_DEF"),

    ATTACK_SPEED("ATTACK_SPEED"),
    CAST_SPEED("CAST_SPEED"),

    PHY_CRIT("PHY_CRIT"),
    MGC_CRIT("MGC_CRIT");


    public final String type;
}