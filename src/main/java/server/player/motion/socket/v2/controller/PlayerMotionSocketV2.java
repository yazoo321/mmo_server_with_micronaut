package server.player.motion.socket.v2.controller;

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.function.Predicate;
import org.reactivestreams.Publisher;
import server.monster.server_integration.service.MobInstanceService;
import server.player.motion.model.PlayerMotionList;
import server.player.motion.model.PlayerMotionMessage;
import server.player.motion.service.PlayerMotionService;

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

        return broadcaster.broadcast(
                String.format("[%s] Joined %s!", playerName, map), isValid(playerName));
    }

    @OnMessage
    public Publisher<PlayerMotionList> onMessage(
            String playerName, String map, PlayerMotionMessage message, WebSocketSession session) {

        if (timeToUpdate(message)) {
            // update the players motion
            playerMotionService.updatePlayerMotion(playerName, message.getMotion());
        }

        PlayerMotionList playerMotionList =
                playerMotionService.getPlayersNearMe(message.getMotion(), playerName);
        return broadcaster.broadcast(playerMotionList, isValid(playerName));
    }

    private boolean timeToUpdate(PlayerMotionMessage message) {
        // message asks you to update
        // last updated at is over 3 seconds
        // and this is not a server
        return (message.getUpdate()
                        || message.getLastUpdatedAt().isBefore(Instant.now().minusMillis(3000)))
                && !message.getIsServer();
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
