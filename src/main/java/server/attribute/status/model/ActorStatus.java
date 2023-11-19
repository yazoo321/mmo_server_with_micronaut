package server.attribute.status.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Serdeable
@ReflectiveAccess
public class ActorStatus {


    String actorId;
    Map<String, Instant> actorStatuses;


    public void removeOldStatuses() {
        actorStatuses.values().removeIf(activeTil -> activeTil != null && activeTil.isBefore(Instant.now()));
    }

}
