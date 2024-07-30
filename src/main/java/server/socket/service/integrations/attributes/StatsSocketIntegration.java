package server.socket.service.integrations.attributes;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.service.StatsService;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class StatsSocketIntegration {

    @Inject StatsService statsService;

    @Inject SocketResponseSubscriber socketResponseSubscriber;


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
}
