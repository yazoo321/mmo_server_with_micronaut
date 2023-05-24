package server.socket.service.synchronisation;


import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Motion;
import server.motion.dto.PlayerMotion;
import server.motion.model.SessionParams;
import server.motion.service.PlayerMotionService;
import server.player.character.dto.Character;
import server.player.character.service.PlayerCharacterService;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class SynchronisePlayerService {

    @Inject
    PlayerMotionService playerMotionService;

    @Inject
    PlayerCharacterService playerCharacterService;

    @Inject
    SocketResponseSubscriber socketResponseSubscriber;


    private static final Integer DEFAULT_DISTANCE_THRESHOLD = 1000;

    public void handleSynchronisePlayers(Motion motion, String playerName, WebSocketSession session) {
        int distanceThreshold = DEFAULT_DISTANCE_THRESHOLD;

        playerMotionService
                .getNearbyPlayersAsync(motion, playerName, distanceThreshold)
                .doAfterSuccess(
                        list -> {
                            if (list == null || list.isEmpty()) {
                                return;
                            }
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
    }

    private void evaluateNewPlayers(Set<String> playerNames, WebSocketSession session) {
        Set<String> previouslyTracked =
                (Set<String>)
                        session.asMap()
                                .getOrDefault(SessionParams.TRACKING_PLAYERS.getType(), Set.of());

        Set<String> newPlayers =
                playerNames.stream()
                        .filter(i -> !previouslyTracked.contains(i))
                        .collect(Collectors.toSet());

        Set<String> lostPlayers =
                previouslyTracked.stream()
                        .filter(i -> !playerNames.contains(i))
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

    private void handleLostPlayers(WebSocketSession session, Set<String> lostPlayers) {
        if (lostPlayers == null || lostPlayers.isEmpty()) {
            return;
        }

        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.REMOVE_PLAYERS.getType())
                        .lostPlayers(lostPlayers)
                        .build();

        session.send(socketResponse).subscribe(socketResponseSubscriber);
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
                                                    SocketResponseType.PLAYER_MOTION_UPDATE
                                                            .getType())
                                            .playerMotion(playerMotionMap)
                                            .playerKeys(playerMotionMap.keySet())
                                            .build();

                            session.send(response).subscribe(socketResponseSubscriber);
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
                                                    SocketResponseType.PLAYER_APPEARANCE.getType())
                                            .playerData(characterMap)
                                            .playerKeys(characterMap.keySet())
                                            .build();
                            session.send(response).subscribe(socketResponseSubscriber);
                        })
                .subscribe();
    }

}
