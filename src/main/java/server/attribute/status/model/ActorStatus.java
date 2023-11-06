package server.attribute.status.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Data
@Serdeable
public class ActorStatus {


    String actorId;
    Map<String, Instant> actorStatuses;


    public void removeOldStatuses() {
        actorStatuses.values().removeIf(activeTil -> activeTil != null && activeTil.isBefore(Instant.now()));
    }

}
