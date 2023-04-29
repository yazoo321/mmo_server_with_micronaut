package server.socket.v1;

import io.micronaut.http.annotation.Header;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import jakarta.inject.Inject;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import server.socket.model.SocketMessage;
import server.socket.service.SocketProcessOutgoingService;

@Slf4j
@ServerWebSocket("/v1/communication-socket")
public class CommunicationSocket {

    // Receive message, process and push to kafka
    // Kafka listener will receive response and will determine who to publish the updates to

    private final WebSocketBroadcaster broadcaster;

    private static final Integer DISTANCE_THRESHOLD = 1000;

    @Inject SocketProcessOutgoingService socketProcessService;

    public CommunicationSocket(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @OnOpen
    public void onOpen(
            WebSocketSession session, @Header String playerName, @Header String serverName) {
        log.info("Session opened for: {} / {}", playerName, serverName);
    }

    @OnMessage
    public void onMessage(
            SocketMessage message,
            WebSocketSession session,
            @Header String playerName,
            @Header String serverName) {
        message.setPlayerName(playerName);
        message.setServerName(serverName);
        socketProcessService.processMessage(message);
    }

    @OnClose
    public void onClose(
            WebSocketSession session, @Header String playerName, @Header String serverName) {
        log.info("session closing for: {} / {}", playerName, serverName);
    }

    private Predicate<WebSocketSession> isValid(String playerOrMob) {
        // we will report to player every time they call update about other players nearby
        return s -> {
            return true;
        };
    }
}
