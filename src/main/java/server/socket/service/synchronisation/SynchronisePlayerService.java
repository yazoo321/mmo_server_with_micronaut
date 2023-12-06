package server.socket.service.synchronisation;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Motion;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.service.EquipItemService;
import server.items.inventory.model.response.GenericInventoryData;
import server.motion.dto.PlayerMotion;
import server.motion.model.SessionParams;
import server.motion.service.PlayerMotionService;
import server.player.model.Character;
import server.player.service.PlayerCharacterService;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class SynchronisePlayerService {

    @Inject PlayerMotionService playerMotionService;

    @Inject PlayerCharacterService playerCharacterService;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    @Inject EquipItemService equipItemService;

    private static final Integer DEFAULT_DISTANCE_THRESHOLD = 1000;

    public void handleSynchronisePlayers(Motion motion, WebSocketSession session) {
        int distanceThreshold = DEFAULT_DISTANCE_THRESHOLD;

        String actorId = SessionParamHelper.getActorId(session);

        playerMotionService
                .getNearbyPlayersAsync(motion, actorId, distanceThreshold)
                .doOnSuccess(
                        list -> {
                            if (SessionParamHelper.getIsServer(session)) {
                                boolean hi=true;
                            }
                            if (list == null || list.isEmpty()) {
                                return;
                            }
                            Set<String> actorIds =
                                    list.stream()
                                            .map(PlayerMotion::getActorId)
                                            .collect(Collectors.toSet());

                            evaluateNewPlayers(actorIds, session);
                            // update the names that we follow
                            session.put(SessionParams.TRACKING_PLAYERS.getType(), actorIds);
                        })
                .doOnError(
                        (error) ->
                                log.error("error getting nearby players, {}", error.getMessage()))
                .subscribe();
    }

    private void evaluateNewPlayers(Set<String> actorIds, WebSocketSession session) {
        Set<String> previouslyTracked = SessionParamHelper.getTrackingPlayers(session);

        Set<String> newPlayers =
                actorIds.stream()
                        .filter(i -> !previouslyTracked.contains(i))
                        .collect(Collectors.toSet());

        Set<String> lostPlayers =
                previouslyTracked.stream()
                        .filter(i -> !actorIds.contains(i))
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
        resolveCharacterEquips(newPlayers, session);
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
                                                            PlayerMotion::getActorId,
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

    private void resolveCharacterEquips(Set<String> newPlayers, WebSocketSession session) {
        equipItemService
                .getEquippedItems(newPlayers)
                .doOnSuccess(
                        equippedItems -> {
                            if (equippedItems == null || equippedItems.isEmpty()) {
                                return;
                            }
                            Map<String, List<EquippedItems>> nameToItems =
                                    equippedItems.stream()
                                            .collect(
                                                    Collectors.groupingBy(
                                                            EquippedItems::getActorId,
                                                            Collectors.mapping(
                                                                    Function.identity(),
                                                                    Collectors.toList())));

                            nameToItems.forEach(
                                    (charName, items) -> {
                                        GenericInventoryData equipData = new GenericInventoryData();
                                        equipData.setEquippedItems(items);
                                        equipData.setActorId(charName);

                                        SocketResponse res =
                                                SocketResponse.builder()
                                                        .inventoryData(equipData)
                                                        .messageType(
                                                                SocketResponseType.ADD_EQUIP_ITEM
                                                                        .getType())
                                                        .build();
                                        session.send(res).subscribe(socketResponseSubscriber);
                                    });
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
