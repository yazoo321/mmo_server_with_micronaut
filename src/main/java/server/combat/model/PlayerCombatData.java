package server.combat.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

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
