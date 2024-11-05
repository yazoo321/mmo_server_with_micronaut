package server.socket.model;

import io.micronaut.context.annotation.Requires;
import io.micronaut.websocket.WebSocketBroadcaster;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Singleton
@Requires(beans = WebSocketBroadcaster.class)
@Slf4j
public class SocketResponseSubscriber implements Subscriber<SocketResponse> {

    private final WebSocketBroadcaster broadcaster;

    public SocketResponseSubscriber(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(SocketResponse socketResponse) {
        // Add any necessary validation or data transformations here
        try {
            broadcaster.broadcast(socketResponse);
        } catch (Exception e) {
            log.error("Error broadcasting message: {}", e.getMessage());
        }
    }

    @Override
    public void onError(Throwable throwable) {
        // Add error handling code here
        log.error("SocketResponseSubscriber encountered an error: {}", throwable.getMessage(), throwable);
    }

    @Override
    public void onComplete() {
        // Add any necessary completion code here
    }
}
