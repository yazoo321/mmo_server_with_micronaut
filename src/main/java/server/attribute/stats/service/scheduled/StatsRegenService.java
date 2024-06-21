package server.attribute.stats.service.scheduled;

import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.service.StatsService;
import server.session.SessionParamHelper;
import server.socket.service.SocketProcessOutgoingService;
import server.socket.v1.CommunicationSocket;

import java.util.concurrent.ConcurrentMap;

@Singleton
@Slf4j
public class StatsRegenService {

    @Inject
    SocketProcessOutgoingService socketService;

    @Inject StatsService statsService;

    @Scheduled(fixedDelay = "1000ms", initialDelay = "30s")
    public void applyRegen() {
        ConcurrentMap<String, WebSocketSession> sessions = socketService.getLiveSessions();

        sessions.values().stream()
                .parallel()
                .forEach(
                        s -> {
                            if (SessionParamHelper.getActorId(s).isBlank()) {
//                                would be too much to log
                                return;
                            }
                            if (SessionParamHelper.getIsServer(s)) {
                                // TODO: later support ability to regen on mobs
                                return;
                            }

                            statsService.applyRegen(SessionParamHelper.getActorId(s));
                        });
    }
}
