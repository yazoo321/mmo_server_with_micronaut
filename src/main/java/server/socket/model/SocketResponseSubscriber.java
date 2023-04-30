package server.socket.model;

import io.micronaut.context.annotation.Requires;
import io.micronaut.websocket.WebSocketBroadcaster;
import jakarta.inject.Singleton;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Singleton
@Requires(beans = WebSocketBroadcaster.class)
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
        broadcaster.broadcast(socketResponse);
    }

    @Override
    public void onError(Throwable throwable) {
        // Add error handling code here
    }

    @Override
    public void onComplete() {
        // Add any necessary completion code here
    }
}
