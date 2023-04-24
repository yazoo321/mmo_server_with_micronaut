package server.player.motion.socket.v1.controller;

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import jakarta.inject.Inject;
import java.util.function.Predicate;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.common.dto.Location2D;
import server.player.motion.model.PlayerMotionList;
import server.player.motion.model.PlayerMotionMessage;
import server.player.motion.service.PlayerMotionService;

@ServerWebSocket("/v1/player-motion/{map}/{playerName}/")
public class PlayerMotionSocket {

    private static final Logger LOG = LoggerFactory.getLogger(PlayerMotionSocket.class);
    private final WebSocketBroadcaster broadcaster;

    private static final Location2D distanceThreshold = new Location2D(30, 30);

    @Inject PlayerMotionService playerMotionService;

    public PlayerMotionSocket(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @OnOpen
    public Publisher<String> onOpen(String map, String playerName, WebSocketSession session) {
        return broadcaster.broadcast(
                String.format("[%s] Joined %s!", playerName, map), isValid(playerName));
    }

    @OnMessage
    public Publisher<PlayerMotionList> onMessage(
            String playerName, String map, PlayerMotionMessage message, WebSocketSession session) {
        if (message.getUpdate()) {
            playerMotionService.updatePlayerMotion(playerName, message.getMotion());
        }

        PlayerMotionList playerMotionList =
                playerMotionService.getPlayersNearMe(message.getMotion(), playerName);
        return broadcaster.broadcast(playerMotionList, isValid(playerName));
    }

    @OnClose
    public Publisher<String> onClose(String playerName, String map, WebSocketSession session) {
        playerMotionService.disconnectPlayer(playerName);
        return broadcaster.broadcast(String.format("[%s] Leaving %s!", playerName, map));
    }

    private Predicate<WebSocketSession> isValid(String playerName) {
        // we will report to player every time they call update about other players nearby
        return s ->
                playerName.equalsIgnoreCase(
                        s.getUriVariables().get("playerName", String.class, null));
    }
}
