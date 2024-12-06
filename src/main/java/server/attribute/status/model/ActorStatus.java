package server.attribute.status.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import server.attribute.status.types.StatusTypes;

@Slf4j
@Data
@Serdeable
@ReflectiveAccess
@AllArgsConstructor
@NoArgsConstructor
public class ActorStatus {

    String actorId;
    Set<Status> actorStatuses;
    boolean add;
    Set<String> statusEffects;


    public Set<Status> removeOldStatuses() {
        Set<Status> removedStatuses = new HashSet<>();
        if (this.actorStatuses == null) {
            this.actorStatuses = new HashSet<>();
        }

//        log.info("running remove old statuses on: {}", this.actorStatuses);
        this.actorStatuses.removeIf(
                status -> {
                    if (status.getExpiration() != null && status.getExpiration().isBefore(Instant.now())) {
                        removedStatuses.add(status);
                        return true;
                    } else return false;
                });

        return removedStatuses;
    }

    public Set<String> aggregateStatusEffects() {
        Set<String> statusEffects = new HashSet<>();
        actorStatuses.forEach(status -> statusEffects.addAll(status.getStatusEffects()));
        this.statusEffects = statusEffects;

        return statusEffects;
    }

    public Map<String, Double> aggregateDerived() {
        Map<String, Double> derived = new HashMap<>();

        actorStatuses.forEach(
                status ->
                        status.getDerivedEffects()
                                .forEach(
                                        (k, v) -> derived.merge(k, v, Double::sum)));

        return derived;
    }

    public Set<Status> getActorStatuses() {
        if (actorStatuses == null) {
            actorStatuses = new HashSet<>();
        }

        return actorStatuses;
    }

    public boolean isDead() {
        for (Status status : getActorStatuses()) {
            if (status.getCategory().equals(StatusTypes.DEAD.getType())) {
                return true;
            }
        }

        return false;
    }

    public boolean canMove() {
        boolean canMove = true;
        canMove = canMove && !isDead();

        return canMove;
    }
}
