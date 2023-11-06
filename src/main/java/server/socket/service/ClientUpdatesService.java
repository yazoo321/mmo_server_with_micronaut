package server.socket.service;

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.*;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.items.equippable.model.EquippedItems;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.model.DroppedItem;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
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

    @Inject
    SessionParamHelper sessionParamHelper;

    public void sendMotionUpdatesToSubscribedClients(PlayerMotion playerMotion) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.PLAYER_MOTION_UPDATE.getType())
                        .playerMotion(Map.of(playerMotion.getPlayerName(), playerMotion))
                        .playerKeys(Set.of(playerMotion.getPlayerName()))
                        .build();

        broadcaster
                .broadcast(socketResponse, listensToMotionUpdate(playerMotion.getPlayerName()))
                .subscribe(socketResponseSubscriber);
    }

    public void notifySessionCombatTooFar(WebSocketSession session) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.COMBAT_TOO_FAR.getType())
                        .build();
        session.send(socketResponse).subscribe(socketResponseSubscriber);
    }

    public void notifySessionCombatNotFacing(WebSocketSession session) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.COMBAT_NOT_FACING.getType())
                        .build();
        session.send(socketResponse).subscribe(socketResponseSubscriber);
    }

    public void notifyServerOfRemovedMobs(Set<String> actorIds) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.REMOVE_MOBS.getType())
                        .lostMobs(actorIds)
                        .build();
        broadcaster.broadcast(socketResponse).subscribe(socketResponseSubscriber);
    }

    public void sendMotionUpdatesToSubscribedClients(Monster monster) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.MOB_MOTION_UPDATE.getType())
                        .monsters(Map.of(monster.getMobInstanceId(), monster))
                        .mobKeys(Set.of(monster.getMobInstanceId()))
                        .build();

        broadcaster
                .broadcast(socketResponse, listensToMotionUpdate(monster.getMobInstanceId()))
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
                        .itemInstanceIds(Set.of(itemInstanceId))
                        .build();

        broadcaster
                .broadcast(socketResponse, listensToItemPickup(itemInstanceId))
                .subscribe(socketResponseSubscriber);
    }

    public void sendItemEquipUpdates(List<EquippedItems> equippedItems) {
        if (equippedItems == null || equippedItems.isEmpty()) {
            return;
        }
        String characterName = equippedItems.get(0).getCharacterName();
        GenericInventoryData equipData = new GenericInventoryData();
        equipData.setCharacterName(characterName);
        equipData.setEquippedItems(equippedItems);
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .inventoryData(equipData)
                        .messageType(SocketResponseType.ADD_EQUIP_ITEM.getType())
                        .build();

        broadcaster
                .broadcast(socketResponse, listensToMotionUpdate(characterName))
                .subscribe(socketResponseSubscriber);
    }

    public void sendItemUnEquipUpdates(String playerName, List<String> itemInstanceIds) {
        if (itemInstanceIds == null || itemInstanceIds.isEmpty()) {
            return;
        }
        GenericInventoryData equipData = new GenericInventoryData();
        equipData.setCharacterName(playerName);
        equipData.setItemInstanceIds(itemInstanceIds);
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .inventoryData(equipData)
                        .messageType(SocketResponseType.REMOVE_EQUIP_ITEM.getType())
                        .build();

        broadcaster
                .broadcast(socketResponse, listensToMotionUpdate(playerName))
                .subscribe(socketResponseSubscriber);
    }

    public void sendStatsUpdates(Stats stats) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.STATS_UPDATE.getType())
                        .stats(stats)
                        .build();

        broadcaster
                .broadcast(socketResponse, notifyStatsFor(stats.getActorId(), stats))
                .subscribe(socketResponseSubscriber);
    }

    private Predicate<WebSocketSession> listensToItemPickup(String itemInstanceId) {
        return s -> {
            String serverName = (String) s.asMap().get(SessionParams.SERVER_NAME.getType());

            if (!serverName.isBlank()) {
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

    private boolean sessionListensToPlayerOrMob(WebSocketSession s, String playerOrMob) {
        boolean isServer = SessionParamHelper.getIsServer(s);
        // server does not track mobs
        Set<String> mobs = isServer ? Set.of() : SessionParamHelper.getTrackingMobs(s);
        Set<String> players = SessionParamHelper.getTrackingPlayers(s);

        return mobs.contains(playerOrMob) || players.contains(playerOrMob);
    }

    private boolean sessionIsThePlayerOrMob(WebSocketSession s, String playerOrMob) {
        String playerName = SessionParamHelper.getPlayerName(s);
        String serverName = SessionParamHelper.getServerName(s);

        return playerName.equalsIgnoreCase(playerOrMob) || serverName.equalsIgnoreCase(playerOrMob);
    }

    private Predicate<WebSocketSession> listensToUpdateFor(String playerOrMob) {
        return s ->
                (sessionIsThePlayerOrMob(s, playerOrMob)
                        || sessionListensToPlayerOrMob(s, playerOrMob));
    }

    // TODO: Consider renaming
    private Predicate<WebSocketSession> notifyStatsFor(String playerOrMob, Stats stats) {
        return s -> {
            boolean isThePlayerOrMob = false;
            if (sessionIsThePlayerOrMob(s, playerOrMob)) {
                isThePlayerOrMob = true;
                // update session cache about stats
                SessionParamHelper.updateDerivedStats(s, stats.getDerivedStats());
            }

            return isThePlayerOrMob || sessionListensToPlayerOrMob(s, playerOrMob);
        };
    }

    private Predicate<WebSocketSession> listensToMotionUpdate(String playerOrMob) {
        // we will report to player every time they call update about other players nearby
        return s -> sessionListensToPlayerOrMob(s, playerOrMob);
    }

    private Predicate<WebSocketSession> serverListeningToActorUpdates(String actorId) {
        // we will report to player every time they call update about other players nearby
        return s -> sessionIsServerAndListensToMob(s, actorId);
    }

    private boolean sessionIsServerAndListensToMob(WebSocketSession s, String actorId) {
        return SessionParamHelper.getIsServer(s)
                && SessionParamHelper.getTrackingMobs(s).contains(actorId);
    }
}
