package server.skills.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.session.SessionParamHelper;
import server.skills.model.Skill;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Singleton
public class CombatSkillsService {

    int GLBL_CD = 500;

    @Inject
    SessionParamHelper sessionParamHelper;

    ObjectMapper objectMapper = new ObjectMapper();


//    CombatData combatData, SkillTarget skillTarget, Skill skill
    public void tryApplySkill(CombatRequest combatRequest) {
        CombatData combatData = sessionParamHelper.getSharedActorCombatData(combatRequest.getActorId());
        Map<Skill, Instant> activatedSkills = combatData.getActivatedSkills();

        // cleanup
        for (Map.Entry<Skill, Instant> entry : activatedSkills.entrySet()) {
            if (entry.getValue().plusMillis(entry.getKey().getCooldown()).isBefore(Instant.now())) {
                activatedSkills.remove(entry.getKey());
            }
        }

        String json = "{\"name\":\"Fireball\"}";
        Skill skill;
        try {
             skill = objectMapper.readValue(json, Skill.class);
        } catch (JsonProcessingException e) {
            // invalid name/id
            log.error("Failed to serialize skill, {}", e.getMessage());
            return;
        }

        if (activatedSkills.containsKey(skill)) {
            // the skill is on CD
            return;
        }

        skill.startSkill(combatData, skillTarget);

    }

}
