package server.skills.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.service.StatsService;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.combat.service.CombatService;
import server.session.SessionParamHelper;
import server.skills.available.destruction.fire.Fireball;
import server.skills.available.factory.DefaultSkillFactory;
import server.skills.available.restoration.heals.BasicHeal;
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

    @Inject StatsService statsService;

    @Inject ActorSkillsRepository actorSkillsRepository;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    ObjectMapper objectMapper = new ObjectMapper();

    @Inject CombatService combatService;

    public CombatSkillsService() {
        objectMapper.registerSubtypes(Fireball.class, BasicHeal.class);
    }

    public void tryApplySkill(CombatRequest combatRequest, WebSocketSession session) {
        validateActorId(session, combatRequest);
        CombatData combatData =
                sessionParamHelper.getSharedActorCombatData(combatRequest.getActorId());
        //        checkCombatDataStatsAvailable(combatData);
        Map<String, Instant> activatedSkills = combatData.getActivatedSkills();
        String skillName = combatRequest.getSkillId();
        Skill skill = skillFactory.createSkill(skillName.toLowerCase());

        if (!skill.canApply(combatData, combatRequest.getSkillTarget())) {
            return;
        }
        activatedSkills.put(skillName, Instant.now());
        sessionParamHelper.setSharedActorCombatData(combatRequest.getActorId(), combatData);
        try {
            skill.startSkill(combatData, combatRequest.getSkillTarget(), session);
        } catch (Exception e) {
            log.error("Failed to start skill, {}", e.getMessage());
        }
    }

    public void getActorAvailableSkills(String actorId, WebSocketSession session) {
        ActorSkills actorSkills = new ActorSkills();
        actorSkills.setActorId(actorId);
        actorSkills.setSkills(List.of(new Fireball(), new BasicHeal()));
        SocketResponse socketResponse = new SocketResponse();
        socketResponse.setActorSkills(actorSkills);
        socketResponse.setMessageType(MessageType.UPDATE_ACTOR_SKILLS.getType());

        session.send(socketResponse).subscribe(socketResponseSubscriber);

        // TODO: Make skills either dynamically evaluated, or taken from repo

        //        actorSkillsRepository.getActorSkills(actorId)
        //                .doOnSuccess(actorSkills -> {
        //                            SocketResponse socketResponse = new SocketResponse();
        //                            socketResponse.setActorSkills(actorSkills);
        //
        // socketResponse.setMessageType(MessageType.UPDATE_ACTOR_SKILLS.getType());
        //
        //
        // session.send(socketResponse).subscribe(socketResponseSubscriber);
        //                })
        //                .doOnError(err -> log.error("Failed to send skills to actor, {}",
        // err.getMessage()))
        //                .subscribe();
    }

    //    private void checkCombatDataStatsAvailable(CombatData combatData) {
    //        // we will try fetch stats if they're not present before they're needed, error-prone
    //        Stats stats = statsService.getStatsFor(combatData.getActorId());
    //        if (combatData.getDerivedStats().size() < 5) {
    //            Stats stats = statsService.getStatsFor(combatData.getActorId()).blockingGet();
    //            Stats.mergeLeft(combatData.getDerivedStats(), stats.getDerivedStats());
    //        }
    //    }

    private void validateActorId(WebSocketSession session, CombatRequest combatRequest) {
        if (SessionParamHelper.getIsPlayer(session)) {
            combatRequest.setActorId(SessionParamHelper.getActorId(session));
            combatRequest.getSkillTarget().setCasterId(SessionParamHelper.getActorId(session));
        }
    }
}
