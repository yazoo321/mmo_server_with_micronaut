package server.socket.service.synchronisation;

import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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

//        Set<String> actorIdSessions = sessions.values().stream().map(SessionParamHelper::getActorId).collect(Collectors.toSet());
//        if (!actorIdSessions.isEmpty()) {
//            log.info("evaluateNearbyPlayers: active sessions: {}", actorIdSessions);
//        }

//        log.info("Running evaluateNearbyPlayers");
        sessions.values().parallelStream()
                .forEach(
                        session -> {
                            if (!SessionParamHelper.getIsServer(session) && !SessionParamHelper.getIsPlayer(session)) {
                                // session is not initialized
                                return;
                            }

                            Motion motion = SessionParamHelper.getIsServer(session) ?
                                    getServerMotion(session) : getPlayerMotion(session);

                            if (motion == null) {
                                log.error(
                                        "Motion unexpectedly null for session with Actor ID: {}",
                                        SessionParamHelper.getActorId(session));
                                return;
                            }
//                            log.info("handle sync players");
                            synchronisePlayerService.handleSynchronisePlayers(motion, session);
//                            log.info("handle sync mobs");
                            synchroniseMobService.handleSynchroniseMobs(motion, session);
//                            log.info("handle sync item drops");
                            synchroniseDroppedItemsService.handleSynchroniseDroppedItems(
                                    motion, session);
                        });
    }

    private Motion getServerMotion(WebSocketSession session) {
         Set<String> trackingMobs = SessionParamHelper.getTrackingMobs(session);
        if (trackingMobs.iterator().hasNext()) {
            String actorId = trackingMobs.iterator().next();
            return actorMotionRepository
                    .fetchActorMotion(actorId)
                    .doOnError(err -> log.error(err.getMessage()))
                    .blockingGet();
        } else {
            return null;
        }
    }

    private Motion getPlayerMotion(WebSocketSession session) {
        return actorMotionRepository
                .fetchActorMotion(
                        SessionParamHelper.getActorId(session))
                .doOnError(err -> log.error(err.getMessage()))
                .blockingGet();
    }
}
