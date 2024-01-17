package server.motion.socket.v1.controller;

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import server.motion.model.MotionMessage;
import server.motion.service.PlayerMotionService;
import server.motion.socket.model.PlayerMotionListSubscriber;

import java.util.function.Predicate;

@Deprecated // use CommunicationSocket instead
@Slf4j
@ServerWebSocket("/v1/player-motion/{map}/{actorId}/")
public class PlayerMotionSocket {

    private final WebSocketBroadcaster broadcaster;

    @Inject PlayerMotionService playerMotionService;

    @Inject PlayerMotionListSubscriber subscriber;

    public PlayerMotionSocket(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @OnOpen
    public Publisher<String> onOpen(String map, String actorId, WebSocketSession session) {
        return broadcaster.broadcast(
                String.format("[%s] Joined %s!", actorId, map), isValid(actorId));
    }

    @OnMessage
    public void onMessage(
            String actorId, String map, MotionMessage message, WebSocketSession session) {
        if (message.getUpdate()) {
            playerMotionService.updatePlayerMotion(actorId, message.getMotion()).subscribe();
        }

        playerMotionService
                .getPlayersNearMe(message.getMotion(), actorId)
                .doOnSuccess(
                        motionList -> {
                            session.send(motionList).subscribe(subscriber);
                        })
                .subscribe();
    }

    @OnClose
    public Publisher<String> onClose(String actorId, String map, WebSocketSession session) {
        playerMotionService.disconnectPlayer(actorId);
        return broadcaster.broadcast(String.format("[%s] Leaving %s!", actorId, map));
    }

    private Predicate<WebSocketSession> isValid(String actorId) {
        // we will report to player every time they call update about other players nearby
        return s ->
                actorId.equalsIgnoreCase(s.getUriVariables().get("actorId", String.class, null));
    }
}
