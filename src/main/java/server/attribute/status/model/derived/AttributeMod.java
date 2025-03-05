package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeEffects;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.Status;
import server.attribute.status.types.StatusTypes;

@Data
@Slf4j
@Serdeable
@JsonTypeName("ATTRIBUTE_MOD")
@EqualsAndHashCode(callSuper = true)
public class AttributeMod extends Status {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public AttributeMod() {
        super();
        this.setCategory("ATTRIBUTE_MOD");
    }

    public AttributeMod(
            Instant expiration,
            String sourceActorId,
            StatsTypes attributeRef,
            Double attributeSum,
            Double attributeMultiplier,
            Integer maxStacks,
            String skillId) {
        this.setId(UUID.randomUUID().toString());
        this.setSkillId(skillId);
        this.setCategory(StatusTypes.ATTRIBUTE_MOD.getType());
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
    }

    public Set<String> defaultStatusEffects() {
        return new HashSet<>(Set.of(getCategory()));
    }

    @Override
    public boolean requiresStatsUpdate() {
        return true;
    }
}
