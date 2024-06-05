package server.attribute.stats.service.scheduled;

import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.service.StatsService;
import server.session.SessionParamHelper;
import server.socket.v1.CommunicationSocket;

@Singleton
@Slf4j
public class StatsRegenService {

    @Inject CommunicationSocket socket;

    @Inject StatsService statsService;

    @Scheduled(fixedDelay = "1000ms", initialDelay = "30s")
    public void applyRegen() {
        ConcurrentSet<WebSocketSession> sessions = socket.getLiveSessions();

        sessions.stream()
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
