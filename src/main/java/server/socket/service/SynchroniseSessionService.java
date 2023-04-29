package server.socket.service;

import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.service.MobInstanceService;
import server.motion.dto.PlayerMotion;
import server.motion.model.SessionParams;
import server.motion.service.PlayerMotionService;
import server.player.character.dto.Character;
import server.player.character.service.PlayerCharacterService;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.v1.CommunicationSocket;

@Slf4j
@Singleton
public class SynchroniseSessionService {
    // we need to synchronise certain data for sessions
    // for example, what characters are nearby

    @Inject CommunicationSocket socket;

    @Inject PlayerMotionService playerMotionService;

    @Inject MobInstanceService mobInstanceService;

    @Inject PlayerCharacterService playerCharacterService;

    @Inject WebSocketBroadcaster broadcaster;

    private final int DISTANCE_THRESHOLD = 1000;

    private void evaluateNewPlayers(Set<String> playerNames, WebSocketSession session) {
        Set<String> previouslyTracked =
                (Set<String>)
                        session.asMap()
                                .getOrDefault(SessionParams.TRACKING_PLAYERS.getType(), Set.of());

        Set<String> newPlayers =
                playerNames.stream()
                        .filter(previouslyTracked::contains)
                        .collect(Collectors.toSet());

        Set<String> lostPlayers =
                previouslyTracked.stream()
                        .filter(playerNames::contains)
                        .collect(Collectors.toSet());

        handleNewPlayers(session, newPlayers);
        handleLostPlayers(session, lostPlayers);
    }

    private void handleNewPlayers(WebSocketSession session, Set<String> newPlayers) {
        if (newPlayers == null || newPlayers.isEmpty()) {
            return;
        }

        resolveCharacterMotion(newPlayers, session);
        resolveCharacterAppearance(newPlayers, session);
    }

    private void resolveCharacterMotion(Set<String> newPlayers, WebSocketSession session) {
        playerMotionService
                .getPlayersMotion(newPlayers)
                .doOnError(
                        error ->
                                log.error(
                                        "Failed to get new players motion, {}", error.getMessage()))
                .doOnSuccess(
                        motionList -> {
                            Map<String, PlayerMotion> playerMotionMap =
                                    motionList.stream()
                                            .collect(
                                                    Collectors.toMap(
                                                            PlayerMotion::getPlayerName,
                                                            Function.identity()));

                            SocketResponse response =
                                    SocketResponse.builder()
                                            .messageType(
                                                    SocketResponseType.NEW_PLAYER_MOTION.getType())
                                            .playerMotion(playerMotionMap)
                                            .build();

                            session.send(response).subscribe(null);
                        })
                .subscribe();
    }

    private void resolveCharacterAppearance(Set<String> newPlayers, WebSocketSession session) {
        playerCharacterService
                .getCharactersByNames(newPlayers)
                .doOnError(
                        error ->
                                log.error(
                                        "Failed to get character (appearance info), {}",
                                        error.getMessage()))
                .doOnSuccess(
                        characterList -> {
                            Map<String, Character> characterMap =
                                    characterList.stream()
                                            .collect(
                                                    Collectors.toMap(
                                                            Character::getName,
                                                            Function.identity()));
                            SocketResponse response =
                                    SocketResponse.builder()
                                            .messageType(
                                                    SocketResponseType.NEW_PLAYER_APPEARANCE
                                                            .getType())
                                            .playerData(characterMap)
                                            .build();

                            session.send(response).subscribe(null);
                        })
                .subscribe();
    }

    private void handleLostPlayers(WebSocketSession session, Set<String> lostPlayers) {
        if (lostPlayers == null || lostPlayers.isEmpty()) {
            return;
        }

        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.REMOVE_PLAYERS.getType())
                        .lostPlayers(lostPlayers)
                        .build();

        session.send(socketResponse).subscribe(null);
    }

    @Scheduled(fixedDelay = "1s")
    public void evaluateNearbyPlayers() {
        ConcurrentSet<WebSocketSession> sessions = socket.getLiveSessions();

        sessions.parallelStream()
                .forEach(
                        session -> {
                            String playerName =
                                    (String)
                                            session.asMap()
                                                    .get(SessionParams.PLAYER_NAME.getType());

                            Motion motion =
                                    (Motion) session.asMap().get(SessionParams.MOTION.getType());
                            if (motion == null) {
                                // possibly the motion is not fully initiated
                                return;
                            }
                            // sync nearby players
                            // TODO: Make these calls via Kafka
                            playerMotionService
                                    .getNearbyPlayersAsync(motion, playerName, DISTANCE_THRESHOLD)
                                    .doAfterSuccess(
                                            list -> {
                                                Set<String> playerNames =
                                                        list.stream()
                                                                .map(PlayerMotion::getPlayerName)
                                                                .collect(Collectors.toSet());

                                                evaluateNewPlayers(playerNames, session);
                                                // update the names that we follow
                                                session.put(
                                                        SessionParams.TRACKING_PLAYERS.getType(),
                                                        playerNames);
                                            })
                                    .doOnError(
                                            (error) ->
                                                    log.error(
                                                            "error getting nearby players, {}",
                                                            error.getMessage()))
                                    .subscribe();

                            // sync nearby mobs
                            mobInstanceService
                                    .getMobsNearby(new Location(motion))
                                    .doAfterSuccess(
                                            mobList -> {
                                                Set<String> mobInstanceIds =
                                                        mobList.stream()
                                                                .map(Monster::getMobInstanceId)
                                                                .collect(Collectors.toSet());
                                                session.put(
                                                        SessionParams.TRACKING_MOBS.getType(),
                                                        mobInstanceIds);
                                            })
                                    .doOnError(
                                            (error) ->
                                                    log.error(
                                                            "error getting nearby mobs, {}",
                                                            error.getMessage()))
                                    .subscribe();
                        });
    }
}
