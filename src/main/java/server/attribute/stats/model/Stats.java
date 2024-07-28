package server.attribute.stats.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
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
    @Serdeable.Serializable private Map<String, Double> statusEffects;

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
            Map<String, Double> statusEffects,
            Integer attributePoints) {
        this.actorId = actorId;
        this.baseStats = baseStats == null ? new HashMap<>() : baseStats;
        this.derivedStats = derivedStats == null ? new HashMap<>() : derivedStats;
        this.itemEffects = itemEffects == null ? new HashMap<>() : itemEffects;
        this.statusEffects = statusEffects == null ? new HashMap<>() : statusEffects;
        this.attributePoints = attributePoints == null ? 0 : attributePoints;
    }

    public Stats() {
        baseStats = new HashMap<>();
        derivedStats = new HashMap<>();
        itemEffects = new HashMap<>();
        statusEffects = new HashMap<>();
    }

    public int getBaseStat(StatsTypes stat) {
        return baseStats.getOrDefault(stat.getType(), 0);
    }

    public Integer addToBase(StatsTypes type, Integer val) {
        Integer updated = baseStats.getOrDefault(type.getType(), 0) + val;
        baseStats.put(type.getType(), updated);

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
        Map<String, Double> updatedDerived = this.getDerivedStats();
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

        updatedDerived.put(StatsTypes.ATTACK_SPEED.getType(), 50.0 + dexterity);
        updatedDerived.put(StatsTypes.CAST_SPEED.getType(), 50.0 + intelligence);
        updatedDerived.put(StatsTypes.PHY_AMP.getType(), 1 + strength * 0.01);
        updatedDerived.put(StatsTypes.MAG_AMP.getType(), 1 + intelligence * 0.01);
        updatedDerived.put(StatsTypes.PHY_CRIT.getType(), 5 + dexterity * 0.1);

        updatedDerived.put(StatsTypes.HP_REGEN.getType(), 0.5 + (stamina / 100));
        updatedDerived.put(StatsTypes.MP_REGEN.getType(), 1.0 + (intelligence / 100));

        // add other effects, such as item and statuses (buffs etc)
        Map<String, Double> otherEffects = mergeStats(itemEffects, statusEffects);
        updatedDerived = mergeStats(updatedDerived, otherEffects);

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
