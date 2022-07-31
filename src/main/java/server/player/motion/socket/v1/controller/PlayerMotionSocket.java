package server.player.motion.socket.v1.controller;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.reactivestreams.Publisher;
import server.player.motion.dto.PlayerMotion;
import server.player.motion.socket.v1.service.PlayerMotionService;

import javax.inject.Inject;

@ServerWebSocket("/v1/player-motion/{map}")
public class PlayerMotionSocket {
    // TODO: This implementation will not work properly if replication is enabled in Kubernetes, consider cache like redis

    @Inject
    PlayerMotionService playerMotionService;

    private WebSocketBroadcaster broadcaster;

    public PlayerMotionSocket(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @OnOpen
    public Publisher<PlayerMotion> onOpen(WebSocketSession session, @Header String playerName, String map, @Body PlayerMotion motion) {
        return playerMotionService.updatePlayerMotion(playerName, motion, broadcaster);
    }


    @OnMessage
    public Publisher<PlayerMotion> onMessage(WebSocketSession session, @Header String playerName, @Body PlayerMotion motion) {
        return playerMotionService.updatePlayerMotion(playerName, motion, broadcaster);
    }

    @OnClose
    public Publisher<PlayerMotion> onClose(
            @Header String playerName,
            String map,
            WebSocketSession session) {

        return playerMotionService.disconnectPlayer(playerName, broadcaster);
    }

}
