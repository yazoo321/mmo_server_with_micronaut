package server.socket.service;

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.items.model.DroppedItem;
import server.motion.model.SessionParams;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@Slf4j
@Singleton
public class WebsocketClientUpdatesService extends ClientUpdatesService {

    // broadcaster is a singleton, so should have the sessions available
    @Inject WebSocketBroadcaster broadcaster;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    public void sendUpdateToListeningPlayers(SocketResponse message, String actorId) {
        broadcaster
                .broadcast(message, sessionIsPlayerAndListensToActor(actorId))
                .subscribe(socketResponseSubscriber);
    }

    public void sendUpdateToListening(SocketResponse message, String actorId) {
        // this is to send message to both, players and mobs, but excluding self.
        broadcaster
                .broadcast(message, sessionListensToActorId(actorId))
                .subscribe(socketResponseSubscriber);
    }

    public void sendUpdateToListeningIncludingServer(SocketResponse message, String actorId) {
        // this is to send message to both, players and mobs, but excluding self.
        broadcaster
                .broadcast(message, sessionListensToActorIdWithServer(actorId))
                .subscribe(socketResponseSubscriber);
    }

    public void sendUpdateToListeningIncludingSelf(SocketResponse message, String actorId) {
        // send message to anyone subscribed to this actor
        broadcaster
                .broadcast(message, sessionListensToActorsOrIsTheActor(actorId))
                .subscribe(socketResponseSubscriber);
    }

    public void sendToSelf(WebSocketSession session, SocketResponse message) {
        session.send(message).subscribe(socketResponseSubscriber);
    }

}
