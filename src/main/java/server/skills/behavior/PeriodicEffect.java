package server.skills.behavior;

import server.combat.model.CombatData;

public interface PeriodicEffect {

    void applyEffectAtInterval(CombatData combatData);

    void applyEffect(CombatData combatData);

}
