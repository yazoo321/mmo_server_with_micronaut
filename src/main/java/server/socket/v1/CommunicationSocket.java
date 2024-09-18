package server.socket.v1;

import io.micronaut.http.HttpRequest;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Motion;
import server.motion.service.PlayerMotionService;
import server.session.SessionParamHelper;
import server.socket.model.SocketMessage;
import server.socket.service.SocketProcessOutgoingService;

import java.net.InetAddress;
import java.net.InetSocketAddress;

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


    @OnOpen
    public void onOpen(WebSocketSession session, HttpRequest<?> request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();

        if (remoteAddress != null) {
            // Get the IP address from the remote address
            InetAddress address = remoteAddress.getAddress();

            // Store the address in the session or use it as needed
            log.info("Opening socket connection, address host: {}", address.getHostAddress());
            SessionParamHelper.setAddress(session, address.getHostAddress());
        } else {
            System.err.println("Remote address is null");
        }
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
        String sessionId;
        if (SessionParamHelper.getIsPlayer(session)) {
            sessionId = SessionParamHelper.getActorId(session);
            if (sessionId == null) {
                log.error("player name should not be null on disconnect");
                return;
            }
            playerMotionService.disconnectPlayer(sessionId);
        } else {
            sessionId = SessionParamHelper.getServerName(session);
            if (sessionId == null) {
                log.error("server name should not be null on disconnect");
            }
        }
        log.info("Disconnecting {}", sessionId);
        socketProcessService.removeActorSession(sessionId);
    }

    public void updateSessionParams(WebSocketSession session, SocketMessage message) {
        // This is currently required to get session actor ID - needs to be refactored to use
        // set session parameters
        // TODO: will require updates to tests to remove this dependency
        if (message.getPlayerMotion() != null
                && motionValid(message.getPlayerMotion().getMotion())) {
            sessionParamHelper.setMotion(session, message.getPlayerMotion().getMotion());
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

}
