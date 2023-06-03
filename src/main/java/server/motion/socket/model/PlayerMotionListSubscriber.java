package server.motion.socket.model;

import io.micronaut.context.annotation.Requires;
import io.micronaut.websocket.WebSocketBroadcaster;
import jakarta.inject.Singleton;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import server.motion.model.PlayerMotionList;
import server.socket.model.SocketResponse;

@Singleton
@Requires(beans = WebSocketBroadcaster.class)
public class PlayerMotionListSubscriber implements Subscriber<PlayerMotionList> {

    private final WebSocketBroadcaster broadcaster;

    public PlayerMotionListSubscriber(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(PlayerMotionList motionList) {
        // Add any necessary validation or data transformations here
        broadcaster.broadcast(motionList);
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
