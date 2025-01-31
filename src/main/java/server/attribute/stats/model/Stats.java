package server.attribute.stats.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.Data;
import server.attribute.common.model.AttributeEffects;
import server.attribute.stats.types.StatsTypes;
import server.common.uuid.UUIDHelper;

@Data
@Builder
@Serdeable
@JsonInclude
@ReflectiveAccess
public class Stats {
    @Serdeable.Serializable private String actorId; // player name or mob id
    @Serdeable.Serializable private Map<String, Integer> baseStats;
    @Serdeable.Serializable private Map<String, Double> derivedStats;
    @Serdeable.Serializable private Map<String, Double> itemEffects;
    @Serdeable.Serializable private Map<String, AttributeEffects> statusEffects;
    @Serdeable.Serializable private Map<String, AttributeEffects> talentEffects;
    @Serdeable.Serializable private Integer attributePoints;

    public Map<String, Double> getDerivedStats() {
        if (this.derivedStats == null) {
            this.derivedStats = new HashMap<>();
        }

        return this.derivedStats;
    }

    public Stats(
            String actorId,
            Map<String, Integer> baseStats,
            Map<String, Double> derivedStats,
            Map<String, Double> itemEffects,
            Map<String, AttributeEffects> statusEffects,
            Map<String, AttributeEffects> talentEffects,
            Integer attributePoints) {
        this.actorId = actorId;
        this.baseStats = baseStats == null ? new HashMap<>() : baseStats;
        this.derivedStats = derivedStats == null ? new HashMap<>() : derivedStats;
        this.itemEffects = itemEffects == null ? new HashMap<>() : itemEffects;
        this.statusEffects = statusEffects == null ? new HashMap<>() : statusEffects;
        this.talentEffects = talentEffects == null ? new HashMap<>() : talentEffects;
        this.attributePoints = attributePoints == null ? 0 : attributePoints;
    }

    public Stats() {
        baseStats = new HashMap<>();
        derivedStats = new HashMap<>();
        itemEffects = new HashMap<>();
        statusEffects = new HashMap<>();
        talentEffects = new HashMap<>();
    }

    public int getBaseStat(StatsTypes stat) {
        return baseStats.getOrDefault(stat.getType(), 0);
    }

    public Integer addToBase(StatsTypes type, Integer val) {
        return addToBase(type.getType(), val);
    }

    public Integer addToBase(String type, Integer val) {
        Integer updated = baseStats.getOrDefault(type, 0) + val;
        baseStats.put(type, updated);

        return updated;
    }

    public Double getDerived(StatsTypes type) {
        return derivedStats.getOrDefault(type.getType(), 0.0);
    }

    public boolean setDerived(StatsTypes type, Double val) {
        // returns whether the value has changed or not
        if (derivedStats.containsKey(type.getType())
                && derivedStats.get(type.getType()).equals(val)) {
            return false;
        } else {
            derivedStats.put(type.getType(), val);
            return true;
        }
    }

    public void setBase(StatsTypes type, int value) {
        baseStats.put(type.getType(), value);
    }

    public Map<String, Double> recalculateDerivedStats() {
        Map<String, Double> updatedDerived = new HashMap<>();
        int strength = getBaseStat(StatsTypes.STR);
        int dexterity = getBaseStat(StatsTypes.DEX);
        int stamina = getBaseStat(StatsTypes.STA);
        int intelligence = getBaseStat(StatsTypes.INT);

        updatedDerived.put(StatsTypes.MAX_HP.getType(), 100.0 + stamina * 10);
        updatedDerived.put(StatsTypes.MAX_MP.getType(), 50.0 + intelligence * 5);

        updatedDerived.put(
                StatsTypes.CURRENT_HP.getType(),
                getDerived(StatsTypes.CURRENT_HP) > updatedDerived.get(StatsTypes.MAX_HP.getType())
                        ? updatedDerived.get(StatsTypes.MAX_HP.getType())
                        : getDerived(StatsTypes.CURRENT_HP));

        updatedDerived.put(
                StatsTypes.CURRENT_MP.getType(),
                getDerived(StatsTypes.CURRENT_MP) > updatedDerived.get(StatsTypes.MAX_MP.getType())
                        ? updatedDerived.get(StatsTypes.MAX_MP.getType())
                        : getDerived(StatsTypes.CURRENT_MP));

        updatedDerived.put(StatsTypes.ATTACK_SPEED.getType(), 50.0 + (dexterity / 2));
        updatedDerived.put(StatsTypes.CAST_SPEED.getType(), 50.0 + intelligence);
        updatedDerived.put(StatsTypes.PHY_AMP.getType(), 1 + strength * 0.01);
        updatedDerived.put(StatsTypes.MAG_AMP.getType(), 1 + intelligence * 0.01);
        updatedDerived.put(StatsTypes.PHY_CRIT.getType(), 5 + dexterity * 0.1);

        updatedDerived.put(StatsTypes.HP_REGEN.getType(), 1.0 + (stamina / 5));
        updatedDerived.put(StatsTypes.MP_REGEN.getType(), 1.0 + (intelligence / 5));

        // evaluate base derived stats when there's no items equipped
        updatedDerived.put(StatsTypes.WEAPON_DAMAGE.getType(), 10.0 + (strength / 12));
        updatedDerived.put(StatsTypes.MAIN_HAND_ATTACK_SPEED.getType(), 2.0);
        // add other effects, such as item and statuses (buffs etc)
        Map<String, Double> otherEffects = mergeSummingStats(itemEffects, statusEffects);
        otherEffects = mergeSummingStats(otherEffects, talentEffects);

        updatedDerived = mergeStats(updatedDerived, otherEffects);

        // handle multipliers from statuses and talents

        updatedDerived = applyMultipliers(updatedDerived, talentEffects);
        updatedDerived = applyMultipliers(updatedDerived, statusEffects);

        // evaluate if new entries are different to old ones
        MapDifference<String, Double> diff = Maps.difference(derivedStats, updatedDerived);
        Map<String, Double> changedStats = new HashMap<>(diff.entriesOnlyOnRight());
        diff.entriesOnlyOnLeft()
                .forEach(
                        (key, val) ->
                                changedStats.put(key, 0.0)); // these values have been 'removed'
        diff.entriesDiffering().forEach((key, val) -> changedStats.put(key, val.rightValue()));

        derivedStats = updatedDerived;

        return changedStats;
    }

    public static Map<String, Double> mergeStats(
            Map<String, Double> left, Map<String, Double> right) {
        Map<String, Double> copy = new HashMap<>(left);
        right.forEach((k, v) -> copy.merge(k, v, Double::sum));

        return copy;
    }

    public static Map<String, Double> mergeSummingStats(
            Map<String, Double> left, Map<String, AttributeEffects> right) {
        Map<String, Double> copy = new HashMap<>(left);
        right.forEach((k, v) -> copy.merge(k, v.getAdditiveModifier(), Double::sum));

        return copy;
    }

    public static Map<String, Double> applyMultipliers(
            Map<String, Double> left, Map<String, AttributeEffects> right) {
        Map<String, Double> copy = new HashMap<>(left);
        right.forEach((k, v) -> copy.merge(k, v.getAdditiveModifier(),
                (a, b) -> a * b));

        return copy;
    }

    public static Map<String, Double> mergeLeft(
            Map<String, Double> left, Map<String, Double> right) {
        right.forEach((k, v) -> left.merge(k, v, Double::sum));

        return left;
    }

    private Double getMaxHp() {
        return getDerived(StatsTypes.MAX_HP);
    }

    public boolean canAct() {
        // TODO: Refactor later to status effects, some cases may allow to act
        return this.getDerived(StatsTypes.CURRENT_HP) > 0;
    }

    public boolean isPlayer() {
        return !UUIDHelper.isValid(actorId);
    }
}
