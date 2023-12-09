package server.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Singleton;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import server.attribute.stats.types.StatsTypes;
import server.combat.model.CombatData;
import server.common.dto.Motion;
import server.configuration.redis.JacksonRedisCodecMotion;
import server.items.equippable.model.EquippedItems;
import server.items.types.ItemType;
import server.motion.model.SessionParams;
import server.session.model.CacheDomains;
import server.session.model.CacheKey;

@Singleton
@NonNull public class SessionParamHelper {

    ObjectMapper objectMapper = new ObjectMapper();

    StatefulRedisConnection<String, Motion> connection;
    RedisCommands<String, Motion> redisMotionCache;

    RedisCommands<String, CombatData> redisCombatDataCache;


    public SessionParamHelper(RedisClient redisClient) {
        connection = redisClient.connect(new JacksonRedisCodecMotion(objectMapper));
        redisMotionCache = connection.sync();
        redisCombatDataCache = connection.sync();
    }

    public Motion getSharedActorMotion(String actorId) {
        return redisMotionCache.get(CacheKey.of(CacheDomains.MOTION, actorId));
    }

    public void setSharedActorMotion(String actorId, Motion motion) {
        redisMotionCache.set(CacheKey.of(CacheDomains.MOTION, actorId), motion);
    }

    public static Motion getMotion(WebSocketSession session) {
        return (Motion) session.asMap().getOrDefault(SessionParams.MOTION.getType(), null);
    }

    public void setMotion(WebSocketSession session, Motion motion, String actorId) {
        setSharedActorMotion(actorId, motion);
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
        session.put(SessionParams.ACTOR_ID.getType(), actorId);
        boolean isServer = false;
        try{
            // can change this to REGEX
            UUID.fromString(actorId);
            isServer = true;
        } catch (IllegalArgumentException exception){
            // then it is a player
        }
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

    public static void updateActorDerivedStats(
            WebSocketSession session, Map<String, Double> derivedStats) {
        Map<String, Double> prev = getActorDerivedStats(session);
        prev.putAll(derivedStats);
        session.put(SessionParams.DERIVED_STATS.getType(), prev);
        updatePlayerCombatData(session);
    }

    public static void setDerivedStats(WebSocketSession session, Map<String, Double> derivedStats) {
        session.put(SessionParams.DERIVED_STATS.getType(), derivedStats);
    }

    public static Map<String, Double> getActorDerivedStats(WebSocketSession session) {
        Map<String, Double> derivedStats =
                (Map<String, Double>) session.asMap().get(SessionParams.DERIVED_STATS.getType());
        if (derivedStats == null) {
            derivedStats = new HashMap<>();
            setDerivedStats(session, derivedStats);
        }

        return derivedStats;
    }


    private static void setCombatData(WebSocketSession session, Map<String, CombatData> combatData) {
        session.put(SessionParams.COMBAT_DATA.getType(), combatData);
    }

    private static Map<String, CombatData> getCombatData(WebSocketSession session) {
        Map<String, CombatData> combatData =
                (Map<String, CombatData>) session.asMap().get(SessionParams.COMBAT_DATA.getType());
        if (combatData == null) {
            combatData = new HashMap<>();
            setCombatData(session, combatData);
        }

        return combatData;
    }

    public static CombatData getActorCombatData(WebSocketSession session, String actorId) {
        Map<String, CombatData> combatDataMap = getCombatData(session);
        CombatData combatData;
        if (combatDataMap.containsKey(actorId)) {
            combatData = combatDataMap.get(actorId);
        } else {
            combatData = new CombatData(actorId);
            combatDataMap.put(actorId, combatData);
        }

        return combatData;
    }

    public static void setActorCombatData(WebSocketSession session, String actorId, CombatData combatData) {
        Map<String, CombatData> combatDataMap = getCombatData(session);
        combatDataMap.put(actorId, combatData);
    }

    public static Map<String, EquippedItems> getEquippedItems(WebSocketSession session) {
        Map<String, EquippedItems> equippedItemsMap =
                (Map<String, EquippedItems>)
                        session.asMap().get(SessionParams.EQUIPPED_ITEMS.getType());

        if (equippedItemsMap == null) {
            return setEquippedItems(session, new ArrayList<>());
        }

        return equippedItemsMap;
    }

    public static void addToEquippedItems(WebSocketSession session, EquippedItems equippedItems) {
        Map<String, EquippedItems> equippedItemsMap = getEquippedItems(session);

        equippedItemsMap.put(equippedItems.getCategory(), equippedItems);

        updatePlayerCombatData(session, equippedItemsMap);
    }

    public static void removeFromEquippedItems(WebSocketSession session, String itemInstanceId) {
        Map<String, EquippedItems> equippedItemsMap = getEquippedItems(session);

        for (String key : equippedItemsMap.keySet()) {
            if (equippedItemsMap
                    .get(key)
                    .getItemInstance()
                    .getItemInstanceId()
                    .equals(itemInstanceId)) {
                equippedItemsMap.remove(key);
            }
        }

        updatePlayerCombatData(session, equippedItemsMap);
    }

    public static Map<String, EquippedItems> setEquippedItems(
            WebSocketSession session, List<EquippedItems> equippedItems) {
        Map<String, EquippedItems> data =
                equippedItems.stream()
                        .collect(Collectors.toMap(EquippedItems::getCategory, Function.identity()));
        session.put(SessionParams.EQUIPPED_ITEMS.getType(), data);

        updatePlayerCombatData(session, data);

        return data;
    }

    private static void updatePlayerCombatData(WebSocketSession session) {
        Map<String, EquippedItems> equippedItemsMap = getEquippedItems(session);
        updatePlayerCombatData(session, equippedItemsMap);
    }

    private static void updatePlayerCombatData(
            WebSocketSession session, Map<String, EquippedItems> equippedItemsMap) {
        CombatData combatData = getActorCombatData(session, getActorId(session));
        EquippedItems mainHand = equippedItemsMap.get(ItemType.WEAPON.getType());
        EquippedItems offHand = equippedItemsMap.get(ItemType.SHIELD.getType());

        combatData.setMainHandAttackSpeed(getBaseSpeed(mainHand));

        if (offHand != null) {
            combatData.setOffhandAttackSpeed(offHand.getBaseAttackSpeed());
        } else {
            combatData.setOffhandAttackSpeed(null);
        }

        Map<String, Double> stats = SessionParamHelper.getActorDerivedStats(session);

        combatData.setActorAttackSpeed(stats.get(StatsTypes.ATTACK_SPEED.getType()));
    }

    private static Double getBaseSpeed(EquippedItems item) {
        if (item != null) {
            return item.getBaseAttackSpeed() != null ? item.getBaseAttackSpeed() : 1.0;
        } else {
            return 1.0;
        }
    }
}
