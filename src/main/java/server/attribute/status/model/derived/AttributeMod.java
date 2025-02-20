package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.attribute.common.model.AttributeEffects;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.Status;

@Data
@Serdeable
@NoArgsConstructor
@JsonTypeName("ATTRIBUTE_MOD")
@EqualsAndHashCode(callSuper = false)
public class AttributeMod extends Status {

    public AttributeMod(
            Instant expiration,
            String sourceActorId,
            StatsTypes attributeRef,
            Double attributeSum,
            Double attributeMultiplier,
            Integer maxStacks,
            String skillId) {
        this.setId(UUID.randomUUID().toString());
        this.setAttributeEffects(
                new HashMap<>(
                        Map.of(
                                attributeRef.getType(),
                                new AttributeEffects(
                                        attributeRef.getType(),
                                        attributeSum,
                                        attributeMultiplier))));
        this.setStatusEffects(defaultStatusEffects());
        this.setExpiration(expiration);
        this.setMaxStacks(maxStacks);
        this.setSourceActor(sourceActorId);
        this.setSkillId(skillId);
        this.setCategory(attributeRef.getType());
    }

    public Set<String> defaultStatusEffects() {
        return new HashSet<>(Set.of(getCategory()));
    }

    @Override
    public boolean requiresStatsUpdate() {
        return true;
    }
}
