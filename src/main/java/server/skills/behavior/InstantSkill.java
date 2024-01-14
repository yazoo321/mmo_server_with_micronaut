package server.skills.behavior;

import server.combat.model.CombatData;
import server.skills.model.SkillTarget;

public interface InstantSkill {

    void instantEffect(CombatData combatData, SkillTarget skillTarget);

}
