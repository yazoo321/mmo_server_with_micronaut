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
import server.skills.available.destruction.fire.Fireball;
import server.skills.available.restoration.heals.BasicHeal;
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

    public CombatSkillsService() {
        objectMapper.registerSubtypes(Fireball.class, BasicHeal.class);
    }


    public void tryApplySkill(CombatRequest combatRequest, WebSocketSession session) {
        CombatData combatData = sessionParamHelper.getSharedActorCombatData(combatRequest.getActorId());
        Map<String, Instant> activatedSkills = combatData.getActivatedSkills();
        String skillName = combatRequest.getSkillId();
        String skillJson = String.format("{\"name\":\"%s\"}", skillName);
        Skill skill;
        try {
             skill = objectMapper.readValue(skillJson, Skill.class);
        } catch (JsonProcessingException e) {
            // invalid name/id
            log.error("Failed to serialize skill, {}", e.getMessage());
            return;
        }

        if (activatedSkills.containsKey(skill.getName())) {
            // the skill is on CD
            return;
        }

        if (!skill.canApply(combatData, combatRequest.getSkillTarget())) {
            return;
        }

        skill.startSkill(combatData, combatRequest.getSkillTarget(), session);
    }

}
