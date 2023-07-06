package server.attribute.stats.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.micronaut.core.annotation.Introspected;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.attribute.stats.types.AttributeTypes;

@Data
@Builder
@Introspected
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

    public int getBaseStat(AttributeTypes stat) {
        return baseStats.getOrDefault(stat, 0);
    }

    public void applyItemEffect(AttributeTypes stat, double value) {
        itemEffects.put(stat.getType(), value);
        recalculateDerivedStats();
    }

    public void removeItemEffect(AttributeTypes stat) {
        itemEffects.remove(stat);
        recalculateDerivedStats();
    }

    public Double getDerived(AttributeTypes type) {
        return derivedStats.getOrDefault(type.getType(), 0.0);
    }

    public Map<String, Double> recalculateDerivedStats() {
        Map<String, Double> updatedDerived = new HashMap<>();
        int strength = getBaseStat(AttributeTypes.STR);
        int dexterity = getBaseStat(AttributeTypes.DEX);
        int stamina = getBaseStat(AttributeTypes.STA);
        int intelligence = getBaseStat(AttributeTypes.INT);

        updatedDerived.put(AttributeTypes.MAX_HP.getType(), 100.0 + stamina * 10);
        updatedDerived.put(AttributeTypes.MAX_MP.getType(), 50.0 + intelligence * 5);

        updatedDerived.put(
                AttributeTypes.CURRENT_HP.getType(),
                getDerived(AttributeTypes.CURRENT_HP)
                                > updatedDerived.get(AttributeTypes.MAX_HP.getType())
                        ? updatedDerived.get(AttributeTypes.MAX_HP.getType())
                        : getDerived(AttributeTypes.CURRENT_HP));

        updatedDerived.put(
                AttributeTypes.CURRENT_MP.getType(),
                getDerived(AttributeTypes.CURRENT_MP)
                                > updatedDerived.get(AttributeTypes.MAX_MP.getType())
                        ? updatedDerived.get(AttributeTypes.MAX_MP.getType())
                        : getDerived(AttributeTypes.CURRENT_MP));

        updatedDerived.put(AttributeTypes.ATTACK_SPEED.getType(), 50.0 + dexterity);
        updatedDerived.put(AttributeTypes.CAST_SPEED.getType(), 50.0 + intelligence);
        updatedDerived.put(AttributeTypes.PHY_AMP.getType(), 1 + strength * 0.01);
        updatedDerived.put(AttributeTypes.MAG_AMP.getType(), 1 + intelligence * 0.01);
        updatedDerived.put(AttributeTypes.PHY_CRIT.getType(), 5 + dexterity * 0.1);

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
}
