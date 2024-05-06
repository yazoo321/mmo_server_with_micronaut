package server.socket.service.synchronisation;

import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Motion;
import server.motion.repository.ActorMotionRepository;
import server.session.SessionParamHelper;
import server.socket.v1.CommunicationSocket;

@Slf4j
@Singleton
public class SynchroniseSessionService {
    // we need to synchronise certain data for sessions
    // for example, what characters are nearby

    @Inject CommunicationSocket socket;

    @Inject SynchronisePlayerService synchronisePlayerService;

    @Inject SynchroniseMobService synchroniseMobService;

    @Inject SynchroniseDroppedItemsService synchroniseDroppedItemsService;

    @Inject ActorMotionRepository actorMotionRepository;

    @Scheduled(fixedDelay = "10s", initialDelay = "10s")
    public void syncMotionCacheToDB() {
        ConcurrentSet<WebSocketSession> sessions = socket.getLiveSessions();
        Set<String> actorsMotion = new HashSet<>();

        sessions.parallelStream()
                .forEach(
                        session -> {
                            actorsMotion.add(SessionParamHelper.getActorId(session));
                        });

        // TODO: Find a way to get sync all these actors to repo in 1 call
    }

    @Scheduled(fixedDelay = "500ms")
    public void evaluateNearbyPlayers() {
        ConcurrentSet<WebSocketSession> sessions = socket.getLiveSessions();

        sessions.parallelStream()
                .forEach(
                        session -> {

                            Motion motion = SessionParamHelper.getIsServer(session) ?
                                    SessionParamHelper.getMotion(session) :
                                    actorMotionRepository
                                            .fetchActorMotion(
                                                    SessionParamHelper.getActorId(session))
                                            .doOnError(err -> log.error(err.getMessage()))
                                            .blockingGet();

                            if (motion == null) {
                                log.error(
                                        "Motion unexpectedly null for session with Actor ID: {}",
                                        SessionParamHelper.getActorId(session));
                                return;
                            }

                            synchronisePlayerService.handleSynchronisePlayers(motion, session);
                            synchroniseMobService.handleSynchroniseMobs(motion, session);
                            synchroniseDroppedItemsService.handleSynchroniseDroppedItems(
                                    motion, session);
                        });
    }
}
