package server.skill.service;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import server.combat.model.CombatRequest;
import server.skills.service.CombatSkillsService;

public class CombatSkillServiceTest {

    @Inject CombatSkillsService combatSkillsService;

    private final String PLAYER_1 = "PLAYER_1";

    @Test
    void testApplySkill() {
        CombatRequest combatRequest = prepareCombatReq();
        //        combatSkillsService.tryApplySkill(combatRequest, );
    }

    private CombatRequest prepareCombatReq() {
        CombatRequest combatRequest = new CombatRequest();
        combatRequest.setActorId(PLAYER_1);

        return combatRequest;
    }
}
