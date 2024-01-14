package server.skills.behavior;

import server.combat.model.CombatData;
import server.skills.model.SkillTarget;

public interface TravelSkill {

    void travel(CombatData combatData, SkillTarget skillTarget, Integer travelSpeed);

}
