package server.attribute.talents.service;

import com.mongodb.client.result.DeleteResult;
import io.micronaut.websocket.WebSocketSession;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.DamageSource;
import server.attribute.stats.model.Stats;
import server.attribute.stats.repository.ActorStatsRepository;
import server.attribute.status.model.ActorStatus;
import server.attribute.talents.model.ActorTalents;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentData;
import server.attribute.talents.model.TalentTree;
import server.attribute.talents.producer.TalentProducer;
import server.attribute.talents.repository.TalentRepository;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class TalentService {

    @Inject TalentProducer talentProducer;

    @Inject TalentRepository talentRepository;

    // TODO: Find a way to remove this dependency.
    @Inject ActorStatsRepository statsRepository;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    public void requestAddStatusToActor(ActorStatus actorStatus) {
        talentProducer.requestAddStatusToActor(actorStatus);
    }

    public void requestStatChange(DamageSource damageSource) {
        talentProducer.requestFlatChange(damageSource);
    }

    public void fetchAllTalentsForTree(WebSocketSession session, String treeName) {
        TalentTree tree = talentRepository.getTalentTreeByName(treeName);

        SocketResponse response = new SocketResponse();
        TalentData talentData =
                TalentData.builder()
                        .talentTree(tree)
                        .actorId(SessionParamHelper.getActorId(session))
                        .build();
        response.setTalentData(talentData);
        response.setMessageType(SocketResponseType.ALL_TALENTS.getType());

        session.send(response).subscribe(socketResponseSubscriber);
    }

    public void fetchActorLearnedTalents(String actorId, WebSocketSession session) {
        // don't rely on actor ID from session, could be mobs
        talentRepository
                .getActorTalents(actorId)
                .doOnSuccess(
                        actorTalents -> {
                            SocketResponse response = new SocketResponse();
                            TalentData talentData =
                                    TalentData.builder()
                                            .actorTalents(actorTalents)
                                            .actorId(actorId)
                                            .build();
                            response.setTalentData(talentData);
                            response.setMessageType(
                                    SocketResponseType.LEARNED_TALENTS.getType());

                            session.send(response).subscribe(socketResponseSubscriber);
                        })
                .doOnError(err -> log.error("Failed to fetch actor talents, {}", err.getMessage()))
                .subscribe();
    }

    public Map<String, Integer> getPointsPerTree(ActorTalents actorTalents) {
        Map<String, Integer> pointsPerTree = new HashMap<>();
        actorTalents
                .getLearnedTalents()
                .forEach((key, value) -> pointsPerTree.merge(key, value, Integer::sum));

        return pointsPerTree;
    }

    public void fetchAvailableTalents(WebSocketSession session) {
        fetchAvailableTalents(SessionParamHelper.getActorId(session))
                .doOnSuccess(
                        availableTalents -> {
                            SocketResponse response = new SocketResponse();
                            TalentData talentData =
                                    TalentData.builder()
                                            .talentLevels(availableTalents)
                                            .actorId(SessionParamHelper.getActorId(session))
                                            .build();
                            response.setTalentData(talentData);
                            response.setMessageType(
                                    SocketResponseType.AVAILABLE_TALENTS.getType());

                            session.send(response).subscribe(socketResponseSubscriber);
                        })
                .subscribe();
    }

    private Single<Map<String, Integer>> fetchAvailableTalents(String actorId) {
        Map<String, Talent> talents = talentRepository.getAllTalents();

        Single<Stats> actorStatsSingle = statsRepository.fetchActorStats(actorId);
        Single<ActorTalents> actorTalentsSingle = talentRepository.getActorTalents(actorId);

        Map<String, Integer> availableTalents = new HashMap<>();
        return Single.zip(
                actorStatsSingle,
                actorTalentsSingle,
                (stats, actorTalents) -> {
                    Map<String, Integer> pointsPerTree = getPointsPerTree(actorTalents);

                    talents.forEach(
                            (talentName, talent) -> {
                                int canLevelTo =
                                        talent.isAvailableForCharacter(
                                                stats,
                                                actorTalents.getLearnedTalents(),
                                                pointsPerTree);
                                if (canLevelTo > 0) {
                                    availableTalents.put(talentName, canLevelTo);
                                }
                            });

                    return availableTalents;
                });
    }

    public void learnTalent(WebSocketSession session, String talentName, int level) {
        String actorId = SessionParamHelper.getActorId(session);
        fetchAvailableTalents(actorId)
                .doOnSuccess(
                        availableToLevel -> {
                            if (!availableToLevel.containsKey(talentName)) {
                                log.warn(
                                        "Actor: {}, tried to learn a talent: {} they haven't met"
                                                + " dependencies for",
                                        actorId,
                                        talentName);
                                SocketResponse socketResponse = new SocketResponse();
                                socketResponse.setMessageType(
                                        SocketResponseType.NOTIFY_ERROR_MESSAGE.getType());
                                socketResponse.setCustomData(
                                        "You do not meet the requirements for this talent");
                                session.send(socketResponse).subscribe(socketResponseSubscriber);
                                return;
                            }

                            int expectedToLevel = availableToLevel.get(talentName);
                            if (level != expectedToLevel) {
                                log.warn(
                                        "Actor {} tried to learn talent: {} with level: {}, but"
                                                + " it's currently level: {}",
                                        actorId,
                                        talentName,
                                        level,
                                        expectedToLevel);
                                // silently fail, in case it's a race condition.
                                return;
                            }

                            // this is locally cached, so is fast
                            talentRepository
                                    .getActorTalents(actorId)
                                    .doOnSuccess(
                                            actorTalents -> {
                                                actorTalents
                                                        .getLearnedTalents()
                                                        .put(talentName, level);
                                                talentRepository
                                                        .insertActorTalents(actorId, actorTalents)
                                                        .doOnSuccess(
                                                                updated -> {
                                                                    SocketResponse response =
                                                                            new SocketResponse();
                                                                    TalentData talentData =
                                                                            TalentData.builder()
                                                                                    .actorTalents(
                                                                                            updated)
                                                                                    .actorId(
                                                                                            actorId)
                                                                                    .build();
                                                                    response.setTalentData(
                                                                            talentData);
                                                                    response.setMessageType(
                                                                            SocketResponseType
                                                                                    .LEARNED_TALENTS
                                                                                    .getType());

                                                                    session.send(response)
                                                                            .subscribe(
                                                                                    socketResponseSubscriber);
                                                                })
                                                        .subscribe();
                                            })
                                    .doOnError(
                                            err ->
                                                    log.error(
                                                            "Error processing in learning talent,"
                                                                    + " {}",
                                                            err.getMessage()))
                                    .subscribe();
                        })
                .subscribe();
    }

    public Single<Map<Talent, Integer>> getActorTalentsOfApplyType(
            String actorId, String applyType) {
        return talentRepository.getActorTalentsOfApplyType(actorId, applyType);
    }

    public Single<ActorTalents> initializeActorTalents(String actorId) {
        ActorTalents talents = new ActorTalents();
        talents.setLearnedTalents(new HashMap<>());
        talents.setActorId(actorId);

        return talentRepository.insertActorTalents(actorId, talents);
    }

    public Single<DeleteResult> deleteActorTalents(String actorId) {
        return talentRepository.deleteActorTalents(actorId);
    }
}
