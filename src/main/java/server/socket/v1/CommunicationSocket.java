package server.socket.v1;

import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.netty.util.internal.ConcurrentSet;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Motion;
import server.motion.model.SessionParams;
import server.motion.service.PlayerMotionService;
import server.socket.model.SocketMessage;
import server.socket.service.SocketProcessOutgoingService;

@Slf4j
@ServerWebSocket("/v1/communication-socket")
public class CommunicationSocket {
    // following principles from:
    // https://www.confluent.io/blog/real-time-gaming-infrastructure-kafka-ksqldb-websockets/
    // Receive message, process and push to kafka
    // Kafka listener will receive response and will determine who to publish the updates to

    @Inject SocketProcessOutgoingService socketProcessService;
    @Inject PlayerMotionService playerMotionService;

    ConcurrentSet<WebSocketSession> socketSessions = new ConcurrentSet<>();

    @OnOpen
    public void onOpen(WebSocketSession session) {
        // TODO: get player/server name via headers
        socketSessions.add(session);
    }

    @OnMessage
    public void onMessage(SocketMessage message, WebSocketSession session) {
        // TODO: get player/server name via injected headers
        updateSessionParams(session, message);

        socketProcessService.processMessage(message, session);
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        socketSessions.remove(session);
        String playerName = (String) session.asMap().get(SessionParams.PLAYER_NAME.getType());
        if (playerName == null) {
            log.error("player name should not be null on disconnect");
            return;
        }
        playerMotionService.disconnectPlayer(playerName);
    }

    private void updateSessionParams(WebSocketSession session, SocketMessage message) {
        String playerName = message.getPlayerName();
        String serverName = message.getServerName();

        // this is temporary, until we get this via open session.
        String sessionPlayerName =
                (String) session.asMap().get(SessionParams.PLAYER_NAME.getType());
        String sessionServerName =
                (String) session.asMap().get(SessionParams.SERVER_NAME.getType());

        if (playerName != null && !playerName.equals(sessionPlayerName)) {
            session.put(SessionParams.PLAYER_NAME.getType(), playerName);
        }
        if (serverName != null && !serverName.equals(sessionServerName)) {
            session.put(SessionParams.SERVER_NAME.getType(), serverName);
        }

        if (message.getPlayerMotion() != null && motionValid(message.getPlayerMotion().getMotion())) {
            session.put(SessionParams.MOTION.getType(), message.getPlayerMotion().getMotion());
        } else if (message.getMonster() != null &&  motionValid(message.getMonster().getMotion())) {
            session.put(SessionParams.MOTION.getType(), message.getMonster().getMotion());
        }
    }

    private boolean motionValid(Motion motion) {
        return motion != null && motion.getMap() != null &&
                !motion.getMap().isBlank() &&
                !motion.getMap().equalsIgnoreCase("false");
    }

    public ConcurrentSet<WebSocketSession> getLiveSessions() {
        return socketSessions;
    }
}
