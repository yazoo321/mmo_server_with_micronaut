package server.socket.service;

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.motion.model.SessionParams;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class ClientUpdatesService {

    // broadcaster is a singleton, so should have the sessions available
    @Inject WebSocketBroadcaster broadcaster;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    public void sendMotionUpdatesToSubscribedClients(PlayerMotion playerMotion) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.PLAYER_MOTION_UPDATE.getType())
                        .playerMotion(Map.of(playerMotion.getPlayerName(), playerMotion))
                        .build();

        broadcaster
                .broadcast(socketResponse, isValid(playerMotion.getPlayerName()))
                .subscribe(socketResponseSubscriber);
    }

    public void sendMotionUpdatesToSubscribedClients(Monster monster) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.MOB_MOTION_UPDATE.getType())
                        .monsters(Map.of(monster.getMobInstanceId(), monster))
                        .build();

        broadcaster
                .broadcast(socketResponse, isValid(monster.getMobInstanceId()))
                .subscribe(socketResponseSubscriber);
    }

    private Predicate<WebSocketSession> isValid(String playerOrMob) {
        // we will report to player every time they call update about other players nearby
        return s -> {
            String serverName = (String) s.asMap().get(SessionParams.SERVER_NAME.getType());
            boolean isServer = serverName != null && !serverName.isBlank();
            // server does not track mobs
            Set<String> mobs = isServer ? Set.of() :
                    (Set<String>)
                            s.asMap().getOrDefault(SessionParams.TRACKING_MOBS.getType(), Set.of());
            Set<String> players =
                    (Set<String>)
                            s.asMap()
                                    .getOrDefault(
                                            SessionParams.TRACKING_PLAYERS.getType(), Set.of());

            return mobs.contains(playerOrMob) || players.contains(playerOrMob);
        };
    }
}
