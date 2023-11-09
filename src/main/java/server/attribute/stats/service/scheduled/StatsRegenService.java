package server.attribute.stats.service.scheduled;

import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import server.attribute.stats.service.StatsService;
import server.session.SessionParamHelper;
import server.socket.v1.CommunicationSocket;

@Singleton
public class StatsRegenService {

    @Inject
    CommunicationSocket socket;

    @Inject
    StatsService statsService;


    @Scheduled(fixedDelay = "200ms")
    public void evaluateNearbyPlayers() {
        ConcurrentSet<WebSocketSession> sessions = socket.getLiveSessions();

        sessions.stream().parallel().forEach(s -> {
            if (SessionParamHelper.getIsServer(s)) {
                // TODO: later support ability to regen on mobs
                return;
            }

            statsService.applyRegen(SessionParamHelper.getPlayerName(s));
        });
    }
}
