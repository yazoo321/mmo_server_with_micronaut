package server.attribute.stats.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.attribute.stats.types.StatsTypes;

@Data
@Builder
@Introspected
@Serdeable
public class Stats {
    private String actorId; // player name or mob id

    private Map<String, Integer> baseStats;
    private Map<String, Double> derivedStats;
    private Map<String, Double> itemEffects;
    private Map<String, Double> statusEffects;

    private Integer attributePoints;

    @BsonCreator
    @JsonCreator
    public Stats(
            @JsonProperty("actorId") @BsonProperty("actorId") String actorId,
            @JsonProperty("baseStats") @BsonProperty("baseStats") Map<String, Integer> baseStats,
            @JsonProperty("derivedStats") @BsonProperty("derivedStats")
                    Map<String, Double> derivedStats,
            @JsonProperty("itemEffects") @BsonProperty("itemEffects")
                    Map<String, Double> itemEffects,
            @JsonProperty("statusEffects") @BsonProperty("statusEffects")
                    Map<String, Double> statusEffects,
            @JsonProperty("attributePoints") @BsonProperty("attributePoints")
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

    public Double getDerived(StatsTypes type) {
        return derivedStats.getOrDefault(type.getType(), 0.0);
    }

    public void setDerived(StatsTypes type, Double val) {
        derivedStats.put(type.getType(), val);
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
        right.forEach(
                (k, v) -> {
                    if (copy.containsKey(k)) {
                        copy.put(k, copy.get(k) + v);
                    } else {
                        copy.put(k, v);
                    }
                });

        return copy;
    }

    public static Map<String, Double> mergeLeft(
            Map<String, Double> left, Map<String, Double> right) {
        right.forEach(
                (k, v) -> {
                    if (left.containsKey(k)) {
                        left.put(k, left.get(k) + v);
                    } else {
                        left.put(k, v);
                    }
                });

        return left;
    }

    private Double getMaxHp() {
        return getDerived(StatsTypes.MAX_HP);
    }

    public boolean canAct() {
        // TODO: Refactor later to status effects, some cases may allow to act
        return this.getDerived(StatsTypes.CURRENT_HP) > 0;
    }
}
