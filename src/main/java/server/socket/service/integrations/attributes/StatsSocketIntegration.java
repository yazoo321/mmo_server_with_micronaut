package server.socket.service.integrations.attributes;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.service.StatsService;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;

@Slf4j
@Singleton
public class StatsSocketIntegration {

    @Inject
    StatsService statsService;

    @Inject
    SocketResponseSubscriber socketResponseSubscriber;

    public void handleFetchStats(String playerName, WebSocketSession session) {
        statsService.getStatsFor(playerName)
                .doOnSuccess(stats -> {
                    SocketResponse response = SocketResponse.builder()
                            .stats(stats)
                            .build();

                    session.send(response).subscribe(socketResponseSubscriber);
                })
                .doOnError(e -> log.error("Failed to fetch stats for {}, {}", playerName, e.getMessage()))
                .subscribe();
    }
}
