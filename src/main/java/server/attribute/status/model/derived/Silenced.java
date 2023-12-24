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
import java.util.Map;
import java.util.Set;

@Data
@Serdeable
@JsonTypeName("SILENCED")
@EqualsAndHashCode(callSuper = false)
public class Silenced extends Status {

    public Silenced(Instant expiration, String sourceId) {
        this.setDerivedEffects(new HashMap<>());
        this.setStatusEffects(defaultStatusEffects());
        this.setExpiration(expiration);
        this.setCanStack(false);
        this.setOrigin(sourceId);
        this.setCategory(StatusTypes.SILENCED.getType());
    }

    public Set<String> defaultStatusEffects() {
        return new HashSet<>(Set.of(StatusTypes.CANNOT_CAST.getType()));
    }

}
