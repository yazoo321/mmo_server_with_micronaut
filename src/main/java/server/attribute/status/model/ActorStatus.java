package server.attribute.status.model;

import lombok.Data;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Data
public class ActorStatus {


    String actorId;
    Map<String, Instant> actorStatuses;


    public void removeOldStatuses() {
        actorStatuses.values().removeIf(activeTil -> activeTil != null && activeTil.isBefore(Instant.now()));
    }

}
