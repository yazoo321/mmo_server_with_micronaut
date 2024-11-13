package server.socket.service.synchronisation;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.service.MobInstanceService;
import server.motion.model.SessionParams;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class SynchroniseMobService {

    @Inject MobInstanceService mobInstanceService;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    public void handleSynchroniseMobs(Motion motion, WebSocketSession session) {
        if (SessionParamHelper.getIsServer(session)) {
            // don't synchronise mobs if its server session
            return;
        }

        mobInstanceService
                .getMobsNearby(new Location(motion), 20_000)
                .doOnSuccess(
                        mobList -> {
                            if (mobList == null || mobList.isEmpty()) {
                                return;
                            }
                            Set<String> actorIds = evaluateNewMobs(mobList, session);

                            session.put(SessionParams.TRACKING_MOBS.getType(), actorIds);
                        })
                .doOnError(
                        (error) -> log.error("error getting nearby mobs, {}", error.getMessage()))
                .subscribe();
    }

    private Set<String> evaluateNewMobs(List<Monster> mobList, WebSocketSession session) {
        Set<String> actorIds =
                mobList.stream().map(Monster::getActorId).collect(Collectors.toSet());

        Set<String> previouslyTracked =
                (Set<String>)
                        session.asMap()
                                .getOrDefault(SessionParams.TRACKING_MOBS.getType(), Set.of());

        Set<Monster> newMobs =
                mobList.stream()
                        .filter(i -> !previouslyTracked.contains(i.getActorId()))
                        .collect(Collectors.toSet());

        handleNewMobs(session, newMobs);

        Set<String> lostMobs =
                previouslyTracked.stream()
                        .filter(i -> !actorIds.contains(i))
                        .collect(Collectors.toSet());

        handleLostMobs(session, lostMobs);

        return actorIds;
    }

    private void handleNewMobs(WebSocketSession session, Set<Monster> mobs) {
        // later we may need additional calls to get attributes etc.
        if (mobs == null || mobs.isEmpty()) {
            return;
        }

        Map<String, Monster> mobMap =
                mobs.stream().collect(Collectors.toMap(Monster::getActorId, Function.identity()));

        SocketResponse response =
                SocketResponse.builder()
                        .messageType(SocketResponseType.MOB_MOTION_UPDATE.getType())
                        .mobKeys(mobMap.keySet())
                        .monsters(mobMap)
                        .build();

        session.send(response).subscribe(socketResponseSubscriber);
    }

    private void handleLostMobs(WebSocketSession session, Set<String> lostMobs) {
        if (lostMobs == null || lostMobs.isEmpty()) {
            return;
        }

        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.REMOVE_MOBS.getType())
                        .lostMobs(lostMobs)
                        .build();

        session.send(socketResponse).subscribe(socketResponseSubscriber);
    }
}
