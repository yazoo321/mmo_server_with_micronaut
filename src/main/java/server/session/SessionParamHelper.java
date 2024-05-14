package server.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.repository.ActorStatsRepository;
import server.combat.model.CombatData;
import server.common.configuration.redis.JacksonCodecCombatData;
import server.common.configuration.redis.JacksonCodecMotion;
import server.common.dto.Motion;
import server.common.uuid.UUIDHelper;
import server.motion.model.SessionParams;
import server.session.model.CacheDomains;
import server.session.model.CacheKey;

@Singleton
@NonNull @Slf4j
public class SessionParamHelper {

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    RedisCommands<String, Motion> motionCache;
    RedisCommands<String, CombatData> combatDataCache;

    @Inject ActorStatsRepository statsRepository;

    public SessionParamHelper(RedisClient redisClient) {
        motionCache = redisClient.connect(new JacksonCodecMotion(objectMapper)).sync();
        combatDataCache = redisClient.connect(new JacksonCodecCombatData(objectMapper)).sync();
    }

    public void setSharedActorCombatData(String actorId, CombatData combatData) {
        combatDataCache.set(CacheKey.of(CacheDomains.COMBAT_DATA, actorId), combatData);
    }

    public CombatData getSharedActorCombatData(String actorId) {
        CombatData combatData = combatDataCache.get(CacheKey.of(CacheDomains.COMBAT_DATA, actorId));
        if (combatData == null) {
            combatData = new CombatData(actorId);
            setSharedActorCombatData(actorId, combatData);
        }

        return combatData;
    }

    public void updateStats(Stats stats) {
        statsRepository.updateStats(stats.getActorId(), stats);
    }

    public static Motion getMotion(WebSocketSession session) {
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
