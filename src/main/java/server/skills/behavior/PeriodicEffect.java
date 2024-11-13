package server.skills.behavior;

import server.combat.model.CombatData;
import server.skills.model.SkillTarget;

public interface PeriodicEffect {

    void applyEffectAtInterval(CombatData combatData, SkillTarget skillTarget);

    void applyEffect(CombatData combatData, SkillTarget skillTarget);

}
