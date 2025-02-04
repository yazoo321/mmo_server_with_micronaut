package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.attribute.status.model.Status;
import server.attribute.status.types.StatusTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Serdeable
@NoArgsConstructor
@JsonTypeName("STUNNED")
@EqualsAndHashCode(callSuper = false)
public class Stunned extends Status {

    public Stunned(Instant expiration, String sourceActorId, String skillId) {
        this.setId(UUID.randomUUID().toString());
        this.setAttributeEffects(new HashMap<>());
        this.setStatusEffects(defaultStatusEffects());
        this.setExpiration(expiration);
        this.setMaxStacks(1);
        this.setOrigin(sourceActorId);
        this.setSkillId(skillId);
        this.setCategory(StatusTypes.STUNNED.getType());
    }

    public Set<String> defaultStatusEffects() {
        return new HashSet<>(
                Set.of(
                        StatusTypes.CANNOT_ACT.getType(),
                        StatusTypes.CANNOT_ATTACK.getType(),
                        StatusTypes.CANNOT_CAST.getType(),
                        StatusTypes.CANNOT_MOVE.getType()));
    }
}
