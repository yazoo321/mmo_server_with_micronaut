package server.skills.service;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import server.combat.model.CombatData;
import server.session.SessionParamHelper;
import server.skills.active.ActiveSkill;
import server.skills.model.Skill;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

@Singleton
public class CombatSkillsService {

    int GLBL_CD = 500;

    @Inject
    SessionParamHelper sessionParamHelper;

//    CombatData combatData, SkillTarget skillTarget, Skill skill
    public void tryApplySkill(WebSocketSession session, ) {
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
