package server.attribute.status.model;

import static server.attribute.status.types.StatusTypes.*;

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

    private String actorId;
    private Set<Status> actorStatuses;
    private boolean add;
    private Set<String> statusEffects;

    public Set<Status> removeOldStatuses() {
        Set<Status> removedStatuses = new HashSet<>();
        if (this.actorStatuses == null) {
            this.actorStatuses = new HashSet<>();
        }

        this.actorStatuses.removeIf(
                status -> {
                    if (status.getExpiration() != null
                            && status.getExpiration().isBefore(Instant.now())) {
                        removedStatuses.add(status);
                        return true;
                    } else return false;
                });

        return removedStatuses;
    }

    public Set<String> aggregateStatusEffects() {
        Set<String> updatedEffects = new HashSet<>();
        if (actorStatuses == null) {
            this.actorStatuses = new HashSet<>();
        }
        actorStatuses.forEach(status -> updatedEffects.addAll(status.getStatusEffects()));
        this.statusEffects = updatedEffects;

        return getStatusEffects();
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
        return !(this.statusEffects.contains(CANNOT_ACT.getType())
                || this.statusEffects.contains(CANNOT_MOVE.getType()));
    }

    public boolean canCast() {
        aggregateStatusEffects();
        return !(this.statusEffects.contains(CANNOT_ACT.getType())
                || this.statusEffects.contains(CANNOT_CAST.getType()));
    }

    public boolean canAttack() {
        aggregateStatusEffects();
        return !(this.statusEffects.contains(CANNOT_ACT.getType())
                || this.statusEffects.contains(CANNOT_ATTACK.getType()));
    }
}
