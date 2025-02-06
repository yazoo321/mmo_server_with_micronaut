package server.combat.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.uuid.UUIDHelper;

@Data
@AllArgsConstructor
@Serdeable
@ReflectiveAccess
@NoArgsConstructor
public class CombatData {

    private String actorId;

    private Instant mainHandLastAttack;
    private Instant offhandLastAttack;

    Map<String, Boolean> attackSent;

    private Set<String> targets;

    private Instant lastHelperNotification;

    boolean isPlayer;

    // key refers to skill name/id
    private Map<String, Instant> activatedSkills;

    private String combatState;

    public CombatData(String actorId) {
        this.setActorId(actorId);
        this.mainHandLastAttack = Instant.now().minusSeconds(20);
        this.offhandLastAttack = Instant.now().minusSeconds(20);
        this.targets = new HashSet<>();
        this.lastHelperNotification = Instant.now().minusSeconds(20);
        this.attackSent = new HashMap<>();
        this.isPlayer = !UUIDHelper.isValid(actorId);
        this.combatState = CombatState.IDLE.getType();
        this.activatedSkills = new HashMap<>();
    }

    // TODO: this will require rework;
    // Consider using PriorityQueue instead
    // This also does not support multiple skill charges, i.e. skill has 3 charges each one 30s cd
    public Map<String, Instant> getActivatedSkills() {
        return activatedSkills == null ? new HashMap<>() : activatedSkills;
    }
}
