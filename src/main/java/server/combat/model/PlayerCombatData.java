package server.combat.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Serdeable
@ReflectiveAccess
public class PlayerCombatData {

    private String actorId;

    private Double mainHandAttackSpeed;
    private Double offhandAttackSpeed;
    private Double characterAttackSpeed;

    private Instant mainHandLastAttack;
    private Instant offhandLastAttack;

    Map<String, Boolean> attackSent;

    private Set<String> targets;

    private Instant lastHelperNotification;

    public PlayerCombatData(String actorId) {
        this.setActorId(actorId);
        this.mainHandAttackSpeed = 0.0;
        this.offhandAttackSpeed = 0.0;
        this.characterAttackSpeed = 0.0;
        this.mainHandLastAttack = Instant.now().minusSeconds(20);
        this.offhandLastAttack = Instant.now().minusSeconds(20);
        this.targets = new HashSet<>();
        this.lastHelperNotification = Instant.now().minusSeconds(20);
        this.attackSent = new HashMap<>();
    }
}
