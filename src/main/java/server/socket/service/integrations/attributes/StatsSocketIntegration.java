package server.socket.service.integrations.attributes;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.service.PlayerLevelStatsService;
import server.attribute.stats.service.StatsService;
import server.attribute.talents.service.TalentService;
import server.session.SessionParamHelper;
import server.skills.service.CombatSkillsService;
import server.socket.model.SocketMessage;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class StatsSocketIntegration {

    @Inject StatsService statsService;

    @Inject PlayerLevelStatsService playerLevelStatsService;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    @Inject CombatSkillsService combatSkillsService;

    @Inject TalentService talentService;

    // TODO: Offload load via kafka

    public void handleFetchStats(String actorId, WebSocketSession session) {
        statsService
                .getStatsFor(actorId)
                .doOnSuccess(
                        stats -> {
                            SocketResponse response =
                                    SocketResponse.builder()
                                            .messageType(SocketResponseType.STATS_UPDATE.getType())
                                            .stats(stats)
                                            .build();

                            session.send(response).subscribe(socketResponseSubscriber);
                        })
                .doOnError(
                        e -> log.error("Failed to fetch stats for {}, {}", actorId, e.getMessage()))
                .subscribe();
    }

    public void handleAddBaseStat(SocketMessage socketMessage, WebSocketSession session) {
        // TODO: increase validation
        if (!socketMessage.getActorId().equalsIgnoreCase(SessionParamHelper.getActorId(session))) {
            log.error(
                    "Invalid request for adding base stat! Session belongs to: {}, socket message:"
                            + " {}",
                    SessionParamHelper.getActorId(session),
                    socketMessage);
            return;
        }
        playerLevelStatsService
                .handleAddBaseStat(socketMessage.getActorId(), socketMessage.getCustomData())
                .doOnSuccess(
                        stats -> {
                            if (PlayerLevelStatsService.isClassValid(
                                    socketMessage.getCustomData())) {
                                // we also want to send updated skills and talents
                                combatSkillsService.fetchAvailableSkillsToLevel(
                                        socketMessage.getActorId(), session);
                                talentService.fetchAvailableTalents(session);
                            }
                        })
                .subscribe();
    }
}
