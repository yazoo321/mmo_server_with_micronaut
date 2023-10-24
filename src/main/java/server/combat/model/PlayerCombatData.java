package server.combat.model;

import java.time.Instant;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerCombatData {

    private String playerName;

    private Double mainHandAttackSpeed;
    private Double offhandAttackSpeed;
    private Double characterAttackSpeed;

    private Instant mainHandLastAttack;
    private Instant offhandLastAttack;

    private Set<String> targets;
}
