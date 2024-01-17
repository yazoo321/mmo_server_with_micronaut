package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import server.attribute.status.model.Status;
import server.attribute.status.types.StatusTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Data
@Serdeable
@JsonTypeName("STUNNED")
@EqualsAndHashCode(callSuper = false)
public class Stunned extends Status {

    public Stunned(Instant expiration, String sourceId) {
        this.setDerivedEffects(new HashMap<>());
        this.setStatusEffects(defaultStatusEffects());
        this.setExpiration(expiration);
        this.setCanStack(false);
        this.setOrigin(sourceId);
        this.setCategory(StatusTypes.STUNNED.getType());
    }

    public Set<String> defaultStatusEffects() {
        return new HashSet<>(Set.of(
                StatusTypes.CANNOT_ACT.getType(),
                StatusTypes.CANNOT_ATTACK.getType(),
                StatusTypes.CANNOT_CAST.getType(),
                StatusTypes.CANNOT_MOVE.getType()
        ));
    }
}