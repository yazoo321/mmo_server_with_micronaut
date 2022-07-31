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

@ServerWebSocket("/v1/test-socket")
public class TestSocketController {


    private WebSocketBroadcaster broadcaster;

    public TestSocketController(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @OnOpen
    public Publisher<String> onOpen(WebSocketSession session) {
        return broadcaster.broadcast("Welcome");
    }


    @OnMessage
    public Publisher<String> onMessage(WebSocketSession session) {
        return  broadcaster.broadcast("Hello");
    }

    @OnClose
    public Publisher<String> onClose(WebSocketSession session) {

        return broadcaster.broadcast("Goodbye");
    }

}
