package server.session;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Motion;
import server.common.uuid.UUIDHelper;
import server.motion.model.SessionParams;
import server.session.model.CacheDomains;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
@Slf4j
@NoArgsConstructor
public class SessionParamHelper {

    @Getter
    @Setter
    public ConcurrentMap<String, WebSocketSession> liveSessions = new ConcurrentHashMap<>();

    public static void setAddress(WebSocketSession session, String address) {
        session.put(CacheDomains.CLIENT_ADDRESS.getDomain(), address);
    }

    public static String getAddress(WebSocketSession session) {
        return (String) session.asMap().get(CacheDomains.CLIENT_ADDRESS.getDomain());
    }

    public static Motion getMotion(WebSocketSession session) {
        // this should only be used by server
        return (Motion) session.asMap().getOrDefault(SessionParams.MOTION.getType(), null);
    }

    public void setMotion(WebSocketSession session, Motion motion) {
        session.put(SessionParams.MOTION.getType(), motion);
    }

    public static Set<String> getTrackingPlayers(WebSocketSession session) {
        Set<String> trackingPlayers =
                (Set<String>) session.asMap().get(SessionParams.TRACKING_PLAYERS.getType());
        if (trackingPlayers == null) {
            trackingPlayers = new HashSet<>();
            setTrackingPlayers(session, trackingPlayers);
        }

        return trackingPlayers;
    }

    public static void setTrackingPlayers(WebSocketSession session, Set<String> trackingPlayers) {
        session.put(SessionParams.TRACKING_PLAYERS.getType(), trackingPlayers);
    }

    public static Set<String> getTrackingMobs(WebSocketSession session) {
        Set<String> trackingMobs =
                (Set<String>) session.asMap().get(SessionParams.TRACKING_MOBS.getType());
        if (trackingMobs == null) {
            trackingMobs = new HashSet<>();
            setTrackingMobs(session, trackingMobs);
        }

        return trackingMobs;
    }

    public static void setTrackingMobs(WebSocketSession session, Set<String> trackingMobs) {
        session.put(SessionParams.TRACKING_MOBS.getType(), trackingMobs);
    }

    public static void addTrackingMobs(WebSocketSession session, Set<String> trackingMobs) {
        Set<String> mobs = getTrackingMobs(session);
        mobs.addAll(trackingMobs);
    }

    public static void setActorId(WebSocketSession session, String actorId) {
        if (actorId == null || actorId.isBlank()) {
            return;
        }
        session.put(SessionParams.ACTOR_ID.getType(), actorId);
        boolean isServer = UUIDHelper.isValid(actorId);

        session.put(SessionParams.IS_PLAYER.getType(), !isServer);
        session.put(SessionParams.IS_SERVER.getType(), isServer);
    }

    public static void setServerName(WebSocketSession session, String serverName) {
        session.put(SessionParams.SERVER_NAME.getType(), serverName);
        if (serverName != null && !serverName.equalsIgnoreCase("false") && !serverName.isBlank()) {
            session.put(SessionParams.IS_SERVER.getType(), true);
            session.put(SessionParams.IS_PLAYER.getType(), false);
        }
    }

    public static String getActorId(WebSocketSession session) {
        return (String) session.asMap().getOrDefault(SessionParams.ACTOR_ID.getType(), "");
    }

    public static String getServerName(WebSocketSession session) {
        return (String) session.asMap().getOrDefault(SessionParams.SERVER_NAME.getType(), "");
    }

    public static boolean getIsPlayer(WebSocketSession session) {
        return (Boolean) session.asMap().getOrDefault(SessionParams.IS_PLAYER.getType(), false);
    }

    public static boolean getIsServer(WebSocketSession session) {
        return (Boolean) session.asMap().getOrDefault(SessionParams.IS_SERVER.getType(), false);
    }

    public static void setDroppedItems(WebSocketSession session, Set<String> droppedItems) {
        session.put(SessionParams.DROPPED_ITEMS.getType(), droppedItems);
    }

    public static Set<String> getDroppedItems(WebSocketSession session) {
        Set<String> droppedItems =
                (Set<String>) session.asMap().get(SessionParams.DROPPED_ITEMS.getType());
        if (droppedItems == null) {
            droppedItems = new HashSet<>();
            setDroppedItems(session, droppedItems);
        }

        return droppedItems;
    }
}
