package server.socket.service;

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.*;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.items.model.DroppedItem;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.motion.model.SessionParams;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class ClientUpdatesService {

    // broadcaster is a singleton, so should have the sessions available
    @Inject WebSocketBroadcaster broadcaster;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    public void sendMotionUpdatesToSubscribedClients(PlayerMotion playerMotion) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.PLAYER_MOTION_UPDATE.getType())
                        .playerMotion(Map.of(playerMotion.getPlayerName(), playerMotion))
                        .playerKeys(Set.of(playerMotion.getPlayerName()))
                        .build();

        broadcaster
                .broadcast(socketResponse, isValid(playerMotion.getPlayerName()))
                .subscribe(socketResponseSubscriber);
    }

    public void sendMotionUpdatesToSubscribedClients(Monster monster) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.MOB_MOTION_UPDATE.getType())
                        .monsters(Map.of(monster.getMobInstanceId(), monster))
                        .mobKeys(Set.of(monster.getMobInstanceId()))
                        .build();

        broadcaster
                .broadcast(socketResponse, isValid(monster.getMobInstanceId()))
                .subscribe(socketResponseSubscriber);
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
                        .itemInstanceIds(List.of(itemInstanceId))
                        .build();

        broadcaster
                .broadcast(socketResponse, listensToItemPickup(itemInstanceId))
                .subscribe(socketResponseSubscriber);
    }

    private Predicate<WebSocketSession> listensToItemPickup(String itemInstanceId) {
        return s -> {
            String serverName = (String) s.asMap().get(SessionParams.SERVER_NAME.getType());

            if (!serverName.isBlank()) {
                return false;
                // servers don't need item updates
            }

            Set<String> trackedItems =
                    (Set<String>)
                            s.asMap()
                                    .getOrDefault(SessionParams.DROPPED_ITEMS.getType(), Set.of());

            if (trackedItems.contains(itemInstanceId)) {
                trackedItems.remove(itemInstanceId);

                return true;
            }

            return false;
        };
    }

    private Predicate<WebSocketSession> listensToItemDrops(DroppedItem droppedItem) {
        return s -> {
            String serverName = (String) s.asMap().get(SessionParams.SERVER_NAME.getType());

            if (serverName != null && !serverName.isBlank()) {
                return false;
                // servers don't need item updates
            }

            Motion motion = (Motion) s.asMap().getOrDefault(SessionParams.MOTION.getType(), null);

            if (motion == null) {
                return false;
            }

            int defaultThresholdDistance = 1000;
            Location location = new Location(motion);
            if (location.withinThreshold(droppedItem.getLocation(), defaultThresholdDistance)) {
                Set<String> trackedItems = (Set<String>) s.asMap()
                        .getOrDefault(SessionParams.DROPPED_ITEMS.getType(), new HashSet<>());

                trackedItems.add(droppedItem.getItemInstance().getItemInstanceId());
                s.put(SessionParams.DROPPED_ITEMS.getType(), trackedItems);

                return true;
            }

            return false;
        };
    }

    private Predicate<WebSocketSession> isValid(String playerOrMob) {
        // we will report to player every time they call update about other players nearby
        return s -> {
            String serverName = (String) s.asMap().get(SessionParams.SERVER_NAME.getType());
            boolean isServer = serverName != null && !serverName.isBlank();
            // server does not track mobs
            Set<String> mobs =
                    isServer
                            ? Set.of()
                            : (Set<String>)
                                    s.asMap()
                                            .getOrDefault(
                                                    SessionParams.TRACKING_MOBS.getType(),
                                                    Set.of());
            Set<String> players =
                    (Set<String>)
                            s.asMap()
                                    .getOrDefault(
                                            SessionParams.TRACKING_PLAYERS.getType(), Set.of());

            return mobs.contains(playerOrMob) || players.contains(playerOrMob);
        };
    }
}
