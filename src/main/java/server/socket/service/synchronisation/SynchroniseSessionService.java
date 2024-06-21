package server.socket.service.synchronisation;

import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import lombok.extern.slf4j.Slf4j;
import server.common.dto.Motion;
import server.motion.repository.ActorMotionRepository;
import server.session.SessionParamHelper;
import server.socket.service.SocketProcessOutgoingService;
import server.socket.v1.CommunicationSocket;

@Slf4j
@Singleton
public class SynchroniseSessionService {
    // we need to synchronise certain data for sessions
    // for example, what characters are nearby

    @Inject
    SocketProcessOutgoingService socketService;

    @Inject SynchronisePlayerService synchronisePlayerService;

    @Inject SynchroniseMobService synchroniseMobService;

    @Inject SynchroniseDroppedItemsService synchroniseDroppedItemsService;

    @Inject ActorMotionRepository actorMotionRepository;

    @Scheduled(fixedDelay = "500ms")
    public void evaluateNearbyPlayers() {
        ConcurrentMap<String, WebSocketSession> sessions = socketService.getLiveSessions();

        sessions.values().parallelStream()
                .forEach(
                        session -> {
                            if (!SessionParamHelper.getIsServer(session) && !SessionParamHelper.getIsPlayer(session)) {
                                // session is not initialized
                                return;
                            }

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
