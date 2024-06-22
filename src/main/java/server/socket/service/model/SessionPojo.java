package server.socket.service.model;

import io.micronaut.websocket.WebSocketSession;
import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;

@Data
public class SessionPojo {

    private WebSocketSession webSocketSession;
    private ConcurrentHashMap<String, Object> udpCache;
}
