package server.combat.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Serdeable
@ReflectiveAccess
public class CombatData {

    private String actorId;

    private Double mainHandAttackSpeed;
    private Double offhandAttackSpeed;
    private Double actorAttackSpeed;

    private Instant mainHandLastAttack;
    private Instant offhandLastAttack;

    Map<String, Boolean> attackSent;

    private Set<String> targets;

    private Instant lastHelperNotification;

    boolean isPlayer;

    public CombatData(String actorId) {
        this.setActorId(actorId);
        this.mainHandAttackSpeed = 0.0;
        this.offhandAttackSpeed = 0.0;
        this.actorAttackSpeed = 0.0;
        this.mainHandLastAttack = Instant.now().minusSeconds(20);
        this.offhandLastAttack = Instant.now().minusSeconds(20);
        this.targets = new HashSet<>();
        this.lastHelperNotification = Instant.now().minusSeconds(20);
        this.attackSent = new HashMap<>();
        try {
            UUID.fromString(actorId);
            isPlayer = false;
        } catch (IllegalArgumentException exception) {
            isPlayer = true;
        }
    }
}
