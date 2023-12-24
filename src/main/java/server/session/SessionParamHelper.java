package server.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.RedisClient;
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
import server.common.configuration.redis.JacksonCodecCombatData;
import server.common.configuration.redis.JacksonCodecMotion;
import server.common.uuid.UUIDHelper;
import server.items.equippable.model.EquippedItems;
import server.items.types.ItemType;
import server.motion.model.SessionParams;
import server.session.model.CacheDomains;
import server.session.model.CacheKey;

@Singleton
@NonNull public class SessionParamHelper {

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

//    StatefulRedisConnection<String, Motion> connectionMotion;
    RedisCommands<String, Motion> motionCache;

    RedisCommands<String, CombatData> combatDataCache;

    public SessionParamHelper(RedisClient redisClient) {
        motionCache = redisClient.connect(new JacksonCodecMotion(objectMapper)).sync();
        combatDataCache = redisClient.connect(new JacksonCodecCombatData(objectMapper)).sync();
    }

    public Motion getSharedActorMotion(String actorId) {
        return motionCache.get(CacheKey.of(CacheDomains.MOTION, actorId));
    }

    public void setSharedActorMotion(String actorId, Motion motion) {
        motionCache.set(CacheKey.of(CacheDomains.MOTION, actorId), motion);
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

    public void setActorDerivedStats(String actorId, Map<String, Double> derivedStats) {
        CombatData combatData = getSharedActorCombatData(actorId);
        combatData.getDerivedStats().putAll(derivedStats);
        setSharedActorCombatData(actorId, combatData);
    }

    public Map<String, Double> getActorDerivedStats(String actorId) {
        CombatData combatData = getSharedActorCombatData(actorId);

        return combatData.getDerivedStats();
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

    public static void setDerivedStats(WebSocketSession session, Map<String, Double> derivedStats) {
        session.put(SessionParams.DERIVED_STATS.getType(), derivedStats);
    }

    public Map<String, EquippedItems> getEquippedItems(WebSocketSession session) {
        Map<String, EquippedItems> equippedItemsMap =
                (Map<String, EquippedItems>)
                        session.asMap().get(SessionParams.EQUIPPED_ITEMS.getType());

        if (equippedItemsMap == null) {
            return setEquippedItems(session, new ArrayList<>());
        }

        return equippedItemsMap;
    }

    public void addToEquippedItems(WebSocketSession session, EquippedItems equippedItems) {
        Map<String, EquippedItems> equippedItemsMap = getEquippedItems(session);

        equippedItemsMap.put(equippedItems.getCategory(), equippedItems);

        updatePlayerCombatData(session, equippedItemsMap);
    }

    public void removeFromEquippedItems(WebSocketSession session, String itemInstanceId) {
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

    public Map<String, EquippedItems> setEquippedItems(
            WebSocketSession session, List<EquippedItems> equippedItems) {
        Map<String, EquippedItems> data =
                equippedItems.stream()
                        .collect(Collectors.toMap(EquippedItems::getCategory, Function.identity()));
        session.put(SessionParams.EQUIPPED_ITEMS.getType(), data);

        updatePlayerCombatData(session, data);

        return data;
    }

    private void updatePlayerCombatData(WebSocketSession session) {
        Map<String, EquippedItems> equippedItemsMap = getEquippedItems(session);
        updatePlayerCombatData(session, equippedItemsMap);
    }

    private void updatePlayerCombatData(
            WebSocketSession session, Map<String, EquippedItems> equippedItemsMap) {
        CombatData combatData = getSharedActorCombatData(getActorId(session));
        Map<String, Double> derivedStats = combatData.getDerivedStats();

        EquippedItems mainHand = equippedItemsMap.get(ItemType.WEAPON.getType());
        EquippedItems offHand = equippedItemsMap.get(ItemType.SHIELD.getType());

        derivedStats.put(StatsTypes.MAIN_HAND_ATTACK_SPEED.getType(), getBaseSpeed(mainHand));

        if (offHand != null) {
            derivedStats.put(StatsTypes.OFF_HAND_ATTACK_SPEED.getType(), offHand.getBaseAttackSpeed());
        }

        setSharedActorCombatData(getActorId(session), combatData);
    }

    private static Double getBaseSpeed(EquippedItems item) {
        if (item != null) {
            return item.getBaseAttackSpeed() != null ? item.getBaseAttackSpeed() : 1.0;
        } else {
            return 1.0;
        }
    }
}
