package server.skills.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.combat.repository.CombatDataCache;
import server.session.SessionParamHelper;
import server.skills.available.cleric.heals.BasicHeal;
import server.skills.available.cleric.heals.HealingRain;
import server.skills.available.mage.fire.Fireball;
import server.skills.available.mage.nature.EclipseBurst;
import server.skills.available.mage.nature.MoonsVengeance;
import server.skills.available.mage.nature.SunSmite;
import server.skills.available.mage.nature.VineGrab;
import server.skills.factory.DefaultSkillFactory;
import server.skills.model.ActorSkills;
import server.skills.model.Skill;
import server.skills.repository.ActorSkillsRepository;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.types.MessageType;

@Slf4j
@Singleton
public class CombatSkillsService {

    int GLBL_CD = 500;

    @Inject SessionParamHelper sessionParamHelper;

    @Inject DefaultSkillFactory skillFactory;

    @Inject ActorSkillsRepository actorSkillsRepository;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    @Inject CombatDataCache combatDataCache;

    ObjectMapper objectMapper = new ObjectMapper();

    public CombatSkillsService() {
        objectMapper.registerSubtypes(Fireball.class, BasicHeal.class);
    }

    public void tryApplySkill(CombatRequest combatRequest, WebSocketSession session) {
        validateActorId(session, combatRequest);

        CombatData combatData = combatDataCache.fetchCombatData(combatRequest.getActorId());
        String skillName = combatRequest.getSkillId();
        Skill skill = skillFactory.createSkill(skillName.toLowerCase());

        skill.tryApply(combatData, combatRequest.getSkillTarget(), session);

        try {
            //            skill.startSkill(combatData, combatRequest.getSkillTarget(), session);
        } catch (Exception e) {
            log.error("Failed to start skill, {}", e.getMessage());
        }
    }

    public void getActorAvailableSkills(String actorId, WebSocketSession session) {
        ActorSkills actorSkills = new ActorSkills();
        actorSkills.setActorId(actorId);
        actorSkills.setSkills(
                List.of(
                        new Fireball(),
                        new BasicHeal(),
                        new HealingRain(),
                        new VineGrab(),
                        new EclipseBurst(),
                        new MoonsVengeance(),
                        new SunSmite()));
        SocketResponse socketResponse = new SocketResponse();
        socketResponse.setActorSkills(actorSkills);
        socketResponse.setMessageType(MessageType.UPDATE_ACTOR_SKILLS.getType());

        session.send(socketResponse).subscribe(socketResponseSubscriber);

        //        actionbarService.getActorActionbar(session);

        // TODO: Make skills either dynamically evaluated, or taken from repo

        //        actorSkillsRepository
        //                .getActorSkills(actorId)
        //                .doOnSuccess(
        //                        actorSkills -> {
        //                            SocketResponse socketResponse = new SocketResponse();
        //                            socketResponse.setActorSkills(actorSkills);
        //
        //                            socketResponse.setMessageType(
        //                                    MessageType.UPDATE_ACTOR_SKILLS.getType());
        //
        //
        // session.send(socketResponse).subscribe(socketResponseSubscriber);
        //
        //
        //                        })
        //                .doOnError(err -> log.error("Failed to send skills to actor, {}",
        // err.getMessage()))
        //                .subscribe();
    }

    private void validateActorId(WebSocketSession session, CombatRequest combatRequest) {
        // TODO: actually validate request.
        if (SessionParamHelper.getIsPlayer(session)) {
            combatRequest.setActorId(SessionParamHelper.getActorId(session));
            combatRequest.getSkillTarget().setCasterId(SessionParamHelper.getActorId(session));
        }
    }
}
