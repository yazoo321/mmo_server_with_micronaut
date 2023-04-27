package server.motion.socket.v2.controller;

import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.netty.util.internal.ConcurrentSet;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

import org.reactivestreams.Publisher;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.service.MobInstanceService;
import server.motion.dto.MotionResult;
import server.motion.dto.PlayerMotion;
import server.motion.model.MotionMessage;
import server.motion.model.SessionParams;
import server.motion.service.PlayerMotionService;

// V2 socket will work quite different to V1.
// We will periodically check the state of who's near the player
// the details of who is near is saved in session
// whenever that player/mob makes any motion, the player will be updated

// TODO: https://www.confluent.io/blog/real-time-gaming-infrastructure-kafka-ksqldb-websockets/
@ServerWebSocket("/v2/actor-updates/{map}/{playerName}/")
public class PlayerMotionSocketV2 {

    private final WebSocketBroadcaster broadcaster;

    private static final Integer DISTANCE_THRESHOLD = 1000;

    @Inject PlayerMotionService playerMotionService;

    @Inject MobInstanceService mobInstanceService;

    ConcurrentSet<WebSocketSession> playerSessionsList;

    public PlayerMotionSocketV2(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
        playerSessionsList = new ConcurrentSet<>();
    }

    @OnOpen
    public Publisher<String> onOpen(String map, String playerName, WebSocketSession session) {
        // player could also be server instance
        session.put(SessionParams.TRACKING_PLAYERS.getType(), List.of());
        session.put(SessionParams.TRACKING_MOBS.getType(), List.of());
        playerSessionsList.add(session);

        return broadcaster.broadcast(
                String.format("[%s] Joined %s!", playerName, map), isValid(playerName));
    }

    @OnMessage
    public Publisher<MotionResult> onMessage(
            String playerName, String map, MotionMessage message, WebSocketSession session) {

        if (timeToUpdate(message, (Instant) session.asMap().get(SessionParams.LAST_UPDATED_AT.getType()))) {
            // update the players motion
            return handlePlayerUpdate(playerName, map, message, session);
        }

        if (null != message.getMobInstanceId() && !message.getMobInstanceId().isBlank()) {
            // knowing whether we create or update is more efficient for db
            if (message.getUpdate()) {
                mobInstanceService.updateMobMotion(message.getMobInstanceId(), message.getMotion());
            } else {
                mobInstanceService.createMob(message.getMobInstanceId(), message.getMotion());
            }
        }

        return null;
    }

    private Publisher<MotionResult> handlePlayerUpdate(String playerName, String map, MotionMessage message, WebSocketSession session) {
        session.put(SessionParams.LAST_UPDATED_AT.getType(), Instant.now());
        PlayerMotion playerMotion = playerMotionService.buildPlayerMotion(playerName, map, message.getMotion());
        session.put(SessionParams.MOTION.getType(), playerMotion.getMotion());
        playerMotionService.updatePlayerMotion(playerMotion).subscribe();

        MotionResult motionResult = MotionResult.builder().playerMotion(playerMotion).build();

        // only broadcast when required
        return broadcaster.broadcast(motionResult, isValid(playerName));
    }

    private boolean timeToUpdate(MotionMessage message, Instant lastUpdated) {
        if (lastUpdated == null) {
            return true;
        }

        return (message.getUpdate() // either server tells us to update (there's been motion)
                || Instant.now().isAfter(lastUpdated.plusMillis(3000))) // or its time to periodically update
                && message.getMobInstanceId() == null; // and it's not a mob
    }

    @OnClose
    public Publisher<String> onClose(String playerName, String map, WebSocketSession session) {
        playerMotionService.disconnectPlayer(playerName);
        playerSessionsList.remove(session);

        return broadcaster.broadcast(String.format("[%s] Leaving %s!", playerName, map));
    }

    private Predicate<WebSocketSession> isValid(String playerOrMob) {
        // we will report to player every time they call update about other players nearby
        return s -> {
            List<String> playersTrackedInSession = (List<String>) s.asMap().get(
                    SessionParams.TRACKING_PLAYERS.getType());

            List<String> mobsTrackedInSession = (List<String>) s.asMap().get(
                    SessionParams.TRACKING_MOBS.getType());

            return playersTrackedInSession.contains(playerOrMob) ||
                    mobsTrackedInSession.contains(playerOrMob);
        };
    }

    @Scheduled(fixedDelay = "1s")
    public void syncNearbyPlayers() {
        playerSessionsList.parallelStream().forEach(session -> {
            String playerName = session.getUriVariables().get("playerName", String.class, null);
            Motion motion = (Motion) session.asMap().get(SessionParams.MOTION.getType());
            if (motion == null) {
                // possibly the motion is not fully initiated
                return;
            }
            // sync nearby players
            playerMotionService.getNearbyPlayersAsync(motion, playerName, DISTANCE_THRESHOLD)
                    .doAfterSuccess(list -> {
                        List<String> names = list.stream().map(PlayerMotion::getPlayerName).toList();
                        // update the names that we follow
                        session.put(SessionParams.TRACKING_PLAYERS.getType(), names);
                    }).subscribe();

            // sync nearby mobs
            mobInstanceService.getMobsNearby(new Location(motion))
                    .doAfterSuccess(mobList -> {
                        List<String> mobInstanceIds = mobList.stream().map(Monster::getMobInstanceId).toList();
                        session.put(SessionParams.TRACKING_MOBS.getType(), mobInstanceIds);
                    }).subscribe();
        });
    }
}
