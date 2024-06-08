package server.socket.service;

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.items.model.DroppedItem;
import server.motion.model.SessionParams;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class ClientUpdatesService {

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

    public void sendDroppedItemUpdates(DroppedItem droppedItem) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.ADD_ITEMS_TO_MAP.getType())
                        .droppedItems(
                                Map.of(
                                        droppedItem.getItemInstance().getItemInstanceId(),
                                        droppedItem))
                        .build();

        broadcaster
                .broadcast(socketResponse, listensToItemDrops(droppedItem))
                .subscribe(socketResponseSubscriber);
    }

    public void sendItemPickupUpdates(String itemInstanceId) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.REMOVE_ITEMS_FROM_MAP.getType())
                        .itemInstanceIds(Set.of(itemInstanceId))
                        .build();

        broadcaster
                .broadcast(socketResponse, listensToItemPickup(itemInstanceId))
                .subscribe(socketResponseSubscriber);
    }

    private Predicate<WebSocketSession> listensToItemPickup(String itemInstanceId) {
        return s -> {
            if (SessionParamHelper.getIsServer(s)) {
                return false;
                // servers don't need item updates
            }

            Set<String> trackedItems = SessionParamHelper.getDroppedItems(s);

            if (trackedItems.contains(itemInstanceId)) {
                trackedItems.remove(itemInstanceId);

                return true;
            }

            return false;
        };
    }

    private Predicate<WebSocketSession> listensToItemDrops(DroppedItem droppedItem) {
        return s -> {
            if (SessionParamHelper.getIsServer(s)) {
                // servers don't need item updates
                return false;
            }
            Motion motion = SessionParamHelper.getMotion(s);

            if (motion == null) {
                return false;
            }

            int defaultThresholdDistance = 1000;
            Location location = new Location(motion);
            if (location.withinThreshold(droppedItem.getLocation(), defaultThresholdDistance)) {
                // automatically make it listen to this items events

                Set<String> trackedItems = SessionParamHelper.getDroppedItems(s);

                trackedItems.add(droppedItem.getItemInstance().getItemInstanceId());
                s.put(SessionParams.DROPPED_ITEMS.getType(), trackedItems);

                return true;
            }

            return false;
        };
    }

    private Predicate<WebSocketSession> sessionIsPlayerAndListensToActor(String playerOrMob) {
        return s -> sessionIsPlayerAndListensToActor(s, playerOrMob);
    }

    private boolean sessionIsPlayerAndListensToActor(WebSocketSession s, String actorId) {
        if (SessionParamHelper.getIsServer(s)) {
            return false;
        }

        Set<String> actorIds = SessionParamHelper.getTrackingMobs(s);
        actorIds.addAll(SessionParamHelper.getTrackingPlayers(s));

        return actorIds.contains(actorId);
    }

    private Predicate<WebSocketSession> sessionListensToActorId(String actorId) {
        return s -> sessionListensToActorId(s, actorId);
    }

    private Predicate<WebSocketSession> sessionListensToActorIdWithServer(String actorId) {
        return s -> sessionListensToActorIdWithServer(s, actorId);
    }

    private boolean sessionListensToActorId(WebSocketSession s, String actorId) {
        boolean isServer = SessionParamHelper.getIsServer(s);
        // server does not track mob updates
        Set<String> actorIds = isServer ? new HashSet<>() : SessionParamHelper.getTrackingMobs(s);
        actorIds.addAll(SessionParamHelper.getTrackingPlayers(s));

        return actorIds.contains(actorId);
    }

    private boolean sessionListensToActorIdWithServer(WebSocketSession s, String actorId) {
        Set<String> actorIds = SessionParamHelper.getTrackingMobs(s);
        actorIds.addAll(SessionParamHelper.getTrackingPlayers(s));

        return actorIds.contains(actorId);
    }

    private Predicate<WebSocketSession> sessionListensToActorsOrIsTheActor(String actorId) {
        return s -> sessionListensToActorsOrIsTheActor(s, actorId);
    }

    private boolean sessionListensToActorsOrIsTheActor(WebSocketSession s, String actorId) {
        return sessionIsTheActor(s, actorId) || sessionListensToActorId(s, actorId);
    }

    private boolean sessionIsTheActor(WebSocketSession s, String playerOrMob) {
        String actorId = SessionParamHelper.getActorId(s);
        String serverName = SessionParamHelper.getServerName(s);

        return actorId.equalsIgnoreCase(playerOrMob) || serverName.equalsIgnoreCase(playerOrMob);
    }
}
