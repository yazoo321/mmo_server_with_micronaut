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
@JsonTypeName("STUNNED")
@EqualsAndHashCode(callSuper = false)
public class Stunned extends Status {

    public Stunned(Instant expiration, String sourceId) {
        this.setId(UUID.randomUUID().toString());
        this.setDerivedEffects(new HashMap<>());
        this.setStatusEffects(defaultStatusEffects());
        this.setExpiration(expiration);
        this.setCanStack(false);
        this.setOrigin(sourceId);
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
