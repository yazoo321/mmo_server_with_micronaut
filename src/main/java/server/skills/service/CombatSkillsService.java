package server.skills.service;

import jakarta.inject.Singleton;
import server.combat.model.CombatData;
import server.skills.active.ActiveSkill;
import server.skills.model.Skill;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

@Singleton
public class CombatSkillsService {

    int GLBL_CD = 500;


    public void tryApplySkill(CombatData combatData, SkillTarget skillTarget, Skill skill) {
        Map<Skill, Instant> activatedSkills = combatData.getActivatedSkills();

        // cleanup
        for (Map.Entry<Skill, Instant> entry : activatedSkills.entrySet()) {
            if (entry.getValue().plusMillis(entry.getKey().getCooldown()).isBefore(Instant.now())) {
                activatedSkills.remove(entry.getKey());
            }
        }

        if (activatedSkills.containsKey(skill)) {
            // the skill is on CD
            return;
        }

        skill.startSkill(combatData, skillTarget);

    }

}
