package server.util.websocket;

import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.annotation.OnMessage;
import server.motion.model.MotionMessage;
import server.socket.model.SocketMessage;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@ClientWebSocket
public abstract class TestWebSocketClient implements AutoCloseable {

    private final Deque<String> messageHistory = new ConcurrentLinkedDeque<>();

    public String getLatestMessage() {
        return messageHistory.peekLast();
    }

    public List<String> getMessagesChronologically() {
        return new ArrayList<>(messageHistory);
    }

    @OnMessage
    void onMessage(String message) {
        messageHistory.add(message);
    }

    public abstract void send(SocketMessage message);

    public abstract void send(MotionMessage message);
}
