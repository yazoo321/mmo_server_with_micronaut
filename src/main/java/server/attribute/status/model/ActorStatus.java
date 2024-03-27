package server.attribute.status.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Serdeable
@ReflectiveAccess
@AllArgsConstructor
public class ActorStatus {

    String actorId;
    Set<Status> actorStatuses;
    boolean add;

    public Set<Status> removeOldStatuses() {
        Set<Status> removedStatuses = new HashSet<>();
        actorStatuses.removeIf(
                status -> {
                    if (status.getExpiration().isBefore(Instant.now())) {
                        removedStatuses.add(status);
                        return true;
                    } else return false;
                });

        return removedStatuses;
    }

    public Set<String> aggregateStatusEffects() {
        Set<String> statusEffects = new HashSet<>();
        actorStatuses.forEach(status -> statusEffects.addAll(status.getStatusEffects()));

        return statusEffects;
    }

    public Map<String, Double> aggregateDerived() {
        Map<String, Double> derived = new HashMap<>();

        actorStatuses.forEach(
                status ->
                        status.getDerivedEffects()
                                .forEach(
                                        (k, v) -> {
                                            derived.merge(k, v, Double::sum);
                                        }));

        return derived;
    }
}
