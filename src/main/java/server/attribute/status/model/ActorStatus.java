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

import static server.attribute.status.types.StatusTypes.*;

@Slf4j
@Data
@Serdeable
@ReflectiveAccess
@AllArgsConstructor
@NoArgsConstructor
public class ActorStatus {

    private String actorId;
    private Set<Status> actorStatuses;
    private boolean add;
    private Set<String> statusEffects;


    public Set<Status> removeOldStatuses() {
        Set<Status> removedStatuses = new HashSet<>();
        if (this.actorStatuses == null) {
            this.actorStatuses = new HashSet<>();
        }

//        log.info("running remove old statuses on: {}", this.actorStatuses);
        this.actorStatuses.removeIf(
                status -> {
                    if (status.getExpiration() != null && status.getExpiration().isBefore(Instant.now())) {
//                        long diff = status.getExpiration().toEpochMilli() - Instant.now().toEpochMilli();
                        removedStatuses.add(status);
                        return true;
                    } else return false;
                });

        return removedStatuses;
    }

    public Set<String> aggregateStatusEffects() {
        Set<String> updatedEffects = new HashSet<>();
        if (actorStatuses == null) {
//            log.info("actor statuses unexpectedly null in aggregate status effects, setting to empty, for actor: {}",
//                    this.actorId);
            this.actorStatuses = new HashSet<>();
        }
        actorStatuses.forEach(status -> updatedEffects.addAll(status.getStatusEffects()));
        this.statusEffects = updatedEffects;

        return getStatusEffects();
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
        aggregateStatusEffects();
        return !(this.statusEffects.contains(CANNOT_ACT.getType()) ||
                this.statusEffects.contains(CANNOT_MOVE.getType()));
    }

    public boolean canCast() {
        aggregateStatusEffects();
        return !(this.statusEffects.contains(CANNOT_ACT.getType()) ||
                this.statusEffects.contains(CANNOT_CAST.getType()));
    }
}
