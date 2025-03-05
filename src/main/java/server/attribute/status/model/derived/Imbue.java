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
@JsonTypeName("Imbue")
@EqualsAndHashCode(callSuper = false)
public class Imbue extends Status {

    public Imbue(
            Instant expiration,
            String sourceActorId,
            StatsTypes imbueType,
            Map<String, AttributeEffects> attributeEffects,
            Integer maxStacks,
            String skillId) {
        this.setId(UUID.randomUUID().toString());
        this.setAttributeEffects(attributeEffects);
        this.setStatusEffects(defaultStatusEffects());
        this.setExpiration(expiration);
        this.setMaxStacks(maxStacks);
        this.setSourceActor(sourceActorId);
        this.setSkillId(skillId);
        this.setCategory(imbueType.getType());
    }

    public Set<String> defaultStatusEffects() {
        return new HashSet<>(Set.of(getCategory()));
    }
}
