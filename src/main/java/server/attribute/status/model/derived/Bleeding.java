package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.Status;
import server.attribute.status.types.StatusTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Data
@Serdeable
@JsonTypeName("BLEEDING")
@EqualsAndHashCode(callSuper = false)
public class Bleeding extends Status {

    public Bleeding(Instant expiration, String sourceId, Double damage) {
        this.setDerivedEffects(new HashMap<>(Map.of(StatsTypes.CURRENT_HP.getType(), damage)));
        this.setStatusEffects(new HashSet<>());
        this.setExpiration(expiration);
        this.setCanStack(true);
        this.setOrigin(sourceId);
        this.setCategory(StatusTypes.BLEEDING.getType());
    }

}
