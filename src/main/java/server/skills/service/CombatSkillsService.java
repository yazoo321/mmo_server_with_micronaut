package server.skills.service;

import jakarta.inject.Singleton;
import server.combat.model.CombatData;
import server.skills.model.Skill;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Map;

@Singleton
public class CombatSkillsService {

    int GLBL_CD = 500;


    public void tryApplySkill(CombatData combatData, SkillTarget skillTarget, Skill skill) {
        Map<Skill, Instant> activatedSkills = combatData.getActivatedSkills();

        activatedSkills.forEach((s,t) -> {
            if (t.plusMillis(s.get))
        });

    }

}
