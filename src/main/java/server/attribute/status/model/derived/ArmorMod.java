package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.attribute.common.model.AttributeEffects;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.Status;
import server.attribute.status.types.StatusTypes;

import java.time.Instant;
import java.util.*;

@Data
@Serdeable
@NoArgsConstructor
@JsonTypeName("MOVE_SLOW")
@EqualsAndHashCode(callSuper = false)
@Deprecated
public class ArmorMod extends Status {

    public ArmorMod(Instant expiration, String sourceActorId, Double armorMultiplier, Integer maxStacks, String skillId) {
        this.setId(UUID.randomUUID().toString());
        this.setAttributeEffects(
                new HashMap<>(
                        Map.of(
                                StatsTypes.DEF.getType(),
                                new AttributeEffects(
                                        StatsTypes.DEF.getType(),
                                        0.0,
                                        armorMultiplier))));
        this.setStatusEffects(defaultStatusEffects());
        this.setExpiration(expiration);
        this.setMaxStacks(maxStacks);
        this.setOrigin(sourceActorId);
        this.setSkillId(skillId);
        this.setCategory(StatusTypes.ARMOR_MOD.getType());
    }

    public Set<String> defaultStatusEffects() {
        return new HashSet<>(Set.of(StatusTypes.ARMOR_MOD.getType()));
    }
}
