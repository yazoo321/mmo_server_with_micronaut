package server.skills.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.DeleteResult;
import io.micronaut.websocket.WebSocketSession;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.service.StatsService;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.combat.repository.CombatDataCache;
import server.session.SessionParamHelper;
import server.skills.available.AvailableSkills;
import server.skills.available.cleric.heals.BasicHeal;
import server.skills.available.mage.fire.Fireball;
import server.skills.factory.DefaultSkillFactory;
import server.skills.model.ActorSkills;
import server.skills.model.Skill;
import server.skills.repository.ActorSkillsRepository;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class CombatSkillsService {

    @Inject DefaultSkillFactory skillFactory;

    @Inject ActorSkillsRepository actorSkillsRepository;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    @Inject CombatDataCache combatDataCache;

    @Inject AvailableSkills availableSkills;

    @Inject StatsService statsService;

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
    }

    public Single<ActorSkills> initialiseSkills(String actorId) {
        ActorSkills actorSkills = new ActorSkills();
        actorSkills.setActorId(actorId);
        actorSkills.setSkills(new ArrayList<>());
        return actorSkillsRepository.createActorSkills(actorSkills);
    }

    public Single<DeleteResult> deleteActorSkills(String actorId) {
        return actorSkillsRepository.deleteActorSkills(actorId);
    }

    public void fetchActorTrainedSkills(String actorId, WebSocketSession session) {
        actorSkillsRepository
                .getActorSkills(actorId)
                .doOnSuccess(
                        actorSkills -> {
                            SocketResponse socketResponse = new SocketResponse();
                            socketResponse.setActorSkills(actorSkills);
                            socketResponse.setMessageType(
                                    SocketResponseType.UPDATE_ACTOR_SKILLS.getType());

                            session.send(socketResponse).subscribe(socketResponseSubscriber);
                        })
                .subscribe();
    }

    public void fetchAllSkills(WebSocketSession session) {
        SocketResponse socketResponse = new SocketResponse();
        ActorSkills actorSkills =
                new ActorSkills(
                        SessionParamHelper.getActorId(session),
                        availableSkills.getAllSkills().values().stream().toList());
        socketResponse.setActorSkills(actorSkills);
        socketResponse.setMessageType(SocketResponseType.ALL_ACTOR_SKILLS.getType());

        session.send(socketResponse).subscribe(socketResponseSubscriber);
    }

    public void fetchAvailableSkillsToLevel(String actorId, WebSocketSession session) {
        Single<ActorSkills> actorSkillsSingle = actorSkillsRepository
                .getActorSkills(actorId);
        Single<Stats> statsSingle = statsService
                .getStatsFor(actorId);

        Single.zip(actorSkillsSingle, statsSingle, (actorSkills, stats) -> {
            Set<String> learnedSkillSet = actorSkills.getSkills().stream().map(Skill::getName)
                    .collect(Collectors.toSet());

            List<Skill> availableSkillsToLevel = availableSkills.getAvailableSkillsForCharacter(stats);
            availableSkillsToLevel = availableSkillsToLevel.stream().filter(s -> !learnedSkillSet.contains(s.getName())).toList();

            SocketResponse socketResponse = new SocketResponse();
            ActorSkills actorSkillsResponse =
                    new ActorSkills(
                            stats.getActorId(), availableSkillsToLevel);
            socketResponse.setActorSkills(actorSkillsResponse);
            socketResponse.setMessageType(
                    SocketResponseType.AVAILABLE_ACTOR_SKILLS.getType());

            session.send(socketResponse).subscribe(socketResponseSubscriber);
            return 1;
        }).subscribe();
//        statsService
//                .getStatsFor(actorId)
//                .doOnSuccess(
//                        stats -> {
//                            SocketResponse socketResponse = new SocketResponse();
//                            ActorSkills actorSkills =
//                                    new ActorSkills(
//                                            stats.getActorId(),
//                                            availableSkills.getAvailableSkillsForCharacter(stats));
//                            socketResponse.setActorSkills(actorSkills);
//                            socketResponse.setMessageType(
//                                    SocketResponseType.AVAILABLE_ACTOR_SKILLS.getType());
//
//                            session.send(socketResponse).subscribe(socketResponseSubscriber);
//                        })
//                .subscribe();
    }

    public void handleLearnSkill(String skillId, WebSocketSession session) {
        String actorId = SessionParamHelper.getActorId(session);
        // TODO: let's only have 1 skill point per level
        statsService
                .getStatsFor(actorId)
                .doOnSuccess(
                        stats -> {
                            // validate that the skill can be learned.
                            Skill skill = skillFactory.createSkill(skillId.toLowerCase());
                            if (!skill.isAvailableForCharacter(stats)) {
                                log.warn(
                                        "Actor: {}, tried to learn a skill: {} they don't have"
                                                + " level for ",
                                        actorId,
                                        skillId);
                                SocketResponse socketResponse = new SocketResponse();
                                socketResponse.setMessageType(
                                        SocketResponseType.NOTIFY_ERROR_MESSAGE.getType());
                                socketResponse.setCustomData(
                                        "You do not meet the requirements for this skill yet");
                                session.send(socketResponse).subscribe(socketResponseSubscriber);
                                return;
                            }

                            // check if the skill is already learnt
                            actorSkillsRepository
                                    .getActorSkills(actorId)
                                    .doOnSuccess(
                                            actorSkills -> {
                                                if (actorSkills.getSkills().stream()
                                                        .anyMatch(
                                                                s ->
                                                                        s.getClass()
                                                                                .equals(
                                                                                        skill
                                                                                                .getClass()))) {
                                                    log.warn(
                                                            "Actor: {}, tried to learn a skill: {}"
                                                                    + " they already have ",
                                                            actorId,
                                                            skillId);
                                                    SocketResponse socketResponse =
                                                            new SocketResponse();
                                                    socketResponse.setMessageType(
                                                            SocketResponseType.NOTIFY_ERROR_MESSAGE
                                                                    .getType());
                                                    socketResponse.setCustomData(
                                                            "You already levelled this skill!");
                                                    session.send(socketResponse)
                                                            .subscribe(socketResponseSubscriber);
                                                    return;
                                                }

                                                // TODO: check if the actor has enough skill points
                                                // to learn this skill.
                                                if (!hasEnoughSkillPointsToLearn(
                                                        stats, actorSkills.getSkills().size())) {
                                                    log.warn(
                                                            "Actor does not have enough skill"
                                                                    + " points to learn skill");
                                                    SocketResponse socketResponse =
                                                            new SocketResponse();
                                                    socketResponse.setMessageType(
                                                            SocketResponseType.NOTIFY_ERROR_MESSAGE
                                                                    .getType());
                                                    socketResponse.setCustomData(
                                                            "You do not have enough skill points to"
                                                                    + " learn this skill");
                                                    session.send(socketResponse)
                                                            .subscribe(socketResponseSubscriber);
                                                    return;
                                                }
                                                actorSkills.getSkills().add(skill);
                                                actorSkillsRepository
                                                        .setActorSkills(actorSkills)
                                                        .doOnSuccess(
                                                                skills -> {
                                                                    SocketResponse socketResponse =
                                                                            new SocketResponse();
                                                                    socketResponse.setActorSkills(
                                                                            skills);
                                                                    socketResponse.setMessageType(
                                                                            SocketResponseType
                                                                                    .UPDATE_ACTOR_SKILLS
                                                                                    .getType());

                                                                    session.send(socketResponse)
                                                                            .subscribe(
                                                                                    socketResponseSubscriber);
                                                                })
                                                        .subscribe();
                                            })
                                    .subscribe();

                            SocketResponse socketResponse = new SocketResponse();
                            session.send(socketResponse).subscribe(socketResponseSubscriber);
                        })
                .subscribe();
    }

    private boolean hasEnoughSkillPointsToLearn(Stats stats, int numberOfSkillsLearned) {
        // assume 1 skill point available per class level
        int total =
                Arrays.stream(ClassTypes.values())
                        .mapToInt(classType -> stats.getBaseStats().getOrDefault(classType.type, 0))
                        .sum();

        return total > numberOfSkillsLearned;
    }

    private void validateActorId(WebSocketSession session, CombatRequest combatRequest) {
        // TODO: actually validate request.
        if (SessionParamHelper.getIsPlayer(session)) {
            combatRequest.setActorId(SessionParamHelper.getActorId(session));
            combatRequest.getSkillTarget().setCasterId(SessionParamHelper.getActorId(session));
        }
    }
}
