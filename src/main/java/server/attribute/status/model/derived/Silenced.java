package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.attribute.status.model.Status;
import server.attribute.status.types.StatusTypes;

@Data
@Serdeable
@NoArgsConstructor
@JsonTypeName("SILENCED")
@EqualsAndHashCode(callSuper = false)
public class Silenced extends Status {

    public Silenced(Instant expiration, String sourceId, String skillId) {
        this.setId(UUID.randomUUID().toString());
        this.setAttributeEffects(new HashMap<>());
        this.setStatusEffects(defaultStatusEffects());
        this.setExpiration(expiration);
        this.setMaxStacks(1);
        this.setSkillId(skillId);
        this.setOrigin(sourceId);
        this.setCategory(StatusTypes.SILENCED.getType());
    }

    public Set<String> defaultStatusEffects() {
        return new HashSet<>(Set.of(StatusTypes.CANNOT_CAST.getType()));
    }
}
