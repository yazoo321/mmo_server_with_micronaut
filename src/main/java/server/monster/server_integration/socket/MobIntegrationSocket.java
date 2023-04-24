package server.monster.server_integration.socket;

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import jakarta.inject.Inject;
import java.util.function.Predicate;
import org.reactivestreams.Publisher;
import server.common.dto.Location2D;
import server.player.motion.model.PlayerMotionList;
import server.player.motion.model.PlayerMotionMessage;
import server.player.motion.service.PlayerMotionService;

@ServerWebSocket("/v1/mob-integration/{map}/{serverInstance}/")
public class MobIntegrationSocket {

    private final WebSocketBroadcaster broadcaster;

    private static final Location2D distanceThreshold = new Location2D(30, 30);

    @Inject PlayerMotionService playerMotionService;

    public MobIntegrationSocket(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @OnOpen
    public Publisher<String> onOpen(String map, String serverInstance, WebSocketSession session) {
        // see what mobs are available around to sync up
        return broadcaster.broadcast(String.format("[%s] Joined %s!", serverInstance, map));
    }

    @OnMessage
    public Publisher<PlayerMotionList> onMessage(
            String serverInstance,
            String map,
            PlayerMotionMessage message,
            WebSocketSession session) {

        if (message.getUpdate()) {
            playerMotionService.updatePlayerMotion(serverInstance, message.getMotion());
        }

        PlayerMotionList playerMotionList =
                playerMotionService.getPlayersNearMe(message.getMotion(), serverInstance);

        return broadcaster.broadcast(playerMotionList);
    }

    @OnClose
    public Publisher<String> onClose(String serverInstance, String map, WebSocketSession session) {

        return broadcaster.broadcast(String.format("[%s] Leaving %s!", serverInstance, map));
    }

    private Predicate<WebSocketSession> isValid(String playerName) {
        // we will report to player every time they call update about other players nearby
        return s ->
                playerName.equalsIgnoreCase(
                        s.getUriVariables().get("playerName", String.class, null));
    }
}
