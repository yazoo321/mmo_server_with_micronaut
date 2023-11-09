package server.attribute.stats.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatsTypes {
    // BASE
    STR("STR"),
    DEX("DEX"),
    INT("INT"),
    STA("STA"),

    MAX_HP("MAX_HP"),
    MAX_MP("MAX_MP"),
    CURRENT_HP("CURRENT_HP"),
    CURRENT_MP("CURRENT_MP"),

    PHY_AMP("PHY_AMP"),
    MAG_AMP("MAG_AMP"),

    BASE_HP_REGEN("BASE_HP_REGEN"),
    HP_REGEN("HP_REGEN"),

    BASE_MP_REGEN("BASE_MP_REGEN"),
    MP_REGEN("MP_REGEN"),

    DEF("DEF"),
    MAG_DEF("MAG_DEF"),

    ATTACK_SPEED("ATTACK_SPEED"),
    CAST_SPEED("CAST_SPEED"),

    BASE_ATTACK_SPEED("BASE_ATTACK_SPEED"),
    ATTACK_DISTANCE("ATTACK_DISTANCE"),

    PHY_CRIT("PHY_CRIT"),
    MGC_CRIT("MGC_CRIT"),

    WEAPON_DAMAGE("WEAPON_DAMAGE"),

    PHY_REDUCTION("PHY_REDUCTION"),
    MGC_REDUCTION("MGC_REDUCTION");

    public final String type;
}
