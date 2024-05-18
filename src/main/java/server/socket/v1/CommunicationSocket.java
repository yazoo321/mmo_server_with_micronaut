package server.socket.v1;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.netty.websocket.NettyWebSocketSession;
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
import server.session.SessionParamHelper;
import server.socket.model.SocketMessage;
import server.socket.service.SocketProcessOutgoingService;
import server.socket.v2.UDPServer;

@Slf4j
@ServerWebSocket("/v1/communication-socket")
public class CommunicationSocket {
    // following principles from:
    // https://www.confluent.io/blog/real-time-gaming-infrastructure-kafka-ksqldb-websockets/
    // Receive message, process and push to kafka
    // Kafka listener will receive response and will determine who to publish the updates to

    @Inject SocketProcessOutgoingService socketProcessService;
    @Inject PlayerMotionService playerMotionService;

    @Inject SessionParamHelper sessionParamHelper;

    @Inject
    UDPServer udpServer;

    private final ConcurrentSet<WebSocketSession> socketSessions = new ConcurrentSet<>();

    @OnOpen
    public void onOpen(WebSocketSession session, HttpRequest<?> request) {
        if (request.getOrigin().isPresent()) {
            udpServer.addValidIp(request.getOrigin().get(), session);
        } else {
            log.warn("request origin is not present");
        }
        // TODO: get player/server name via headers
        socketSessions.add(session);
    }

    @OnMessage
    public void onMessage(SocketMessage message, WebSocketSession session) {
        // TODO: get player/server name via injected headers
        try {
            updateSessionParams(session, message);
            socketProcessService.processMessage(message, session);
        } catch (Exception e) {
            // avoid closing connection
            log.error("Caught an unhandled exception! {}", e.getMessage());
        }
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        socketSessions.remove(session);
        String actorId = (String) session.asMap().get(SessionParams.ACTOR_ID.getType());
        if (actorId == null) {
            log.error("player name should not be null on disconnect");
            return;
        }
        playerMotionService.disconnectPlayer(actorId);
    }

    public void updateSessionParams(WebSocketSession session, SocketMessage message) {
        // This is currently required to get session actor ID - needs to be refactored to use
        // set session parameters
        // TODO: will require updates to tests to remove this dependency
        if (message.getPlayerMotion() != null
                && motionValid(message.getPlayerMotion().getMotion())) {
                        sessionParamHelper.setMotion(
                                session,
                                message.getPlayerMotion().getMotion());
        } else if (message.getMonster() != null && motionValid(message.getMonster().getMotion())) {
            sessionParamHelper.setMotion(session, message.getMonster().getMotion());
        }
    }

    private boolean motionValid(Motion motion) {
        return motion != null
                && motion.getMap() != null
                && !motion.getMap().isBlank()
                && !motion.getMap().equalsIgnoreCase("false");
    }

    public ConcurrentSet<WebSocketSession> getLiveSessions() {
        return socketSessions;
    }
}
