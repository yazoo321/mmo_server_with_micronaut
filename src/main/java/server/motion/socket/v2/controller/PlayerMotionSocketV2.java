package server.motion.socket.v2.controller;

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

import org.reactivestreams.Publisher;
import server.monster.server_integration.service.MobInstanceService;
import server.motion.dto.PlayerMotion;
import server.motion.model.PlayerMotionList;
import server.motion.model.PlayerMotionMessage;
import server.motion.model.SessionParams;
import server.motion.service.PlayerMotionService;

// V2 socket will work quite different to V1.
// We will periodically check the state of who's near the player
// the details of who is near is saved in session
// whenever that player/mob makes any motion, the player will be updated
@ServerWebSocket("/v2/player-motion/{map}/{playerName}/")
public class PlayerMotionSocketV2 {

    private final WebSocketBroadcaster broadcaster;

    private static final Integer distanceThreshold = 1000;

    @Inject PlayerMotionService playerMotionService;

    @Inject MobInstanceService mobInstanceService;

    public PlayerMotionSocketV2(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @OnOpen
    public Publisher<String> onOpen(String map, String playerName, WebSocketSession session) {
        // player could also be server instance
        session.put(SessionParams.TRACKING_PLAYERS.getType(), List.of());

        return broadcaster.broadcast(
                String.format("[%s] Joined %s!", playerName, map), isValid(playerName));
    }

    @OnMessage
    public Publisher<PlayerMotion> onMessage(
            String playerName, String map, PlayerMotionMessage message, WebSocketSession session) {
        String last_updated = SessionParams.LAST_UPDATED_AT.getType();
        // TODO, get last updated at here..
        if (timeToUpdate(message, (Instant) session.asMap().get(SessionParams.LAST_UPDATED_AT.getType()))) {
            // update the players motion
            session.put(SessionParams.LAST_UPDATED_AT.getType(), Instant.now());
            playerMotionService.updatePlayerMotion(playerName, message.getMotion()).subscribe();
        }
        PlayerMotion playerMotion = new PlayerMotion(playerName, message.getMotion(), true, Instant.now());

        PlayerMotionList playerMotionList =
                playerMotionService.getPlayersNearMe(message.getMotion(), playerName);
        synchroniseMotionForPlayers(playerMotionList, session);

        return broadcaster.broadcast(playerMotion, isValid(playerName));
    }

    private boolean timeToUpdate(PlayerMotionMessage message, Instant lastUpdated) {
        if (lastUpdated == null) {
            return true;
        }
        return ((message.getUpdate() ||
                lastUpdated.isBefore(Instant.now().minusMillis(3000))
                        && !message.getIsMob()));
    }

    @OnClose
    public Publisher<String> onClose(String playerName, String map, WebSocketSession session) {
        playerMotionService.disconnectPlayer(playerName);
        return broadcaster.broadcast(String.format("[%s] Leaving %s!", playerName, map));
    }

    private Predicate<WebSocketSession> isValid(String playerName) {
        // we will report to player every time they call update about other players nearby
        return s -> {
            return ((List<String> ) s.asMap().get(SessionParams.TRACKING_PLAYERS.getType()))
                    .contains(playerName);
        };
    }

    private void synchroniseMotionForPlayers(PlayerMotionList playerMotionList, WebSocketSession session) {
        List<String> playerNames = playerMotionList.getPlayerMotionList()
                .stream().map(PlayerMotion::getPlayerName).toList();
        session.put(SessionParams.TRACKING_PLAYERS.getType(), playerNames);
    }
}
