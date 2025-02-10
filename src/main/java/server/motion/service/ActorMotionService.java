package server.motion.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import server.common.uuid.UUIDHelper;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.motion.model.MotionMessage;
import server.motion.producer.PlayerMotionUpdateProducer;
import server.motion.repository.ActorMotionRepository;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.service.UdpClientUpdateService;
import server.socket.service.WebsocketClientUpdatesService;
import server.utils.FeatureFlag;

@Slf4j
@Singleton
public class ActorMotionService {

    @Inject PlayerMotionUpdateProducer playerMotionUpdateProducer;

    @Inject ActorMotionRepository actorMotionRepository;

    @Inject WebsocketClientUpdatesService websocketClientUpdatesService;
    @Inject UdpClientUpdateService udpClientUpdateService;

    @Inject FeatureFlag featureFlag;

    public void relayForceUpdateActorMotion(MotionMessage motionMessage) {
        // This uses kafka to relay the message to all other relevant nodes
        playerMotionUpdateProducer.sendForceUpdateActorMotion(motionMessage);
        actorMotionRepository.updateActorMotion(
                motionMessage.getActorId(), motionMessage.getMotion());
    }

    public void handleRelayActorMotion(MotionMessage motionMessage) {
        // this is received by ALL nodes, which will send the updates to relevant player clients
        String actorId = motionMessage.getActorId();

        SocketResponse socketResponse =
                UUIDHelper.isPlayer(actorId)
                        ? buildPlayerMotionSocketResponse(motionMessage)
                        : buildMonsterMotionSocketResponse(motionMessage);

        // key difference here to "player-motion-update-result" kafka topic, is that here we send
        // update to self also
        // and we force update, which causes the players client to teleport the player
        if (featureFlag.getEnableUdp()) {
            udpClientUpdateService.sendUpdateToListeningIncludingSelf(socketResponse, actorId);
        } else {
            websocketClientUpdatesService.sendUpdateToListeningIncludingSelf(
                    socketResponse, actorId);
        }
    }

    public void handleRelayPlayerMotion(PlayerMotion playerMotion) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.PLAYER_MOTION_UPDATE.getType())
                        .playerMotion(Map.of(playerMotion.getActorId(), playerMotion))
                        .playerKeys(Set.of(playerMotion.getActorId()))
                        .build();

        //        log.info("Received player motion update result");
        //        log.info("{}", playerMotion);

        if (featureFlag.getEnableUdp()) {
            udpClientUpdateService.sendUpdateToListening(socketResponse, playerMotion.getActorId());
        } else {
            websocketClientUpdatesService.sendUpdateToListening(
                    socketResponse, playerMotion.getActorId());
        }
    }

    public void handleRelayMobMotion(Monster monster) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.MOB_MOTION_UPDATE.getType())
                        .monsters(Map.of(monster.getActorId(), monster))
                        .mobKeys(Set.of(monster.getActorId()))
                        .build();

        //        log.info("{}", monster);

        if (featureFlag.getEnableUdp()) {
            udpClientUpdateService.sendUpdateToListening(socketResponse, monster.getActorId());
        } else {
            websocketClientUpdatesService.sendUpdateToListening(
                    socketResponse, monster.getActorId());
        }
    }

    private SocketResponse buildPlayerMotionSocketResponse(MotionMessage motionMessage) {
        SocketResponse response = new SocketResponse();
        response.setMessageType(SocketResponseType.FORCE_UPDATE_MOTION.getType());

        PlayerMotion updatedPlayerMotion = new PlayerMotion();
        updatedPlayerMotion.setActorId(motionMessage.getActorId());
        updatedPlayerMotion.setMotion(motionMessage.getMotion());
        updatedPlayerMotion.setIsOnline(true);
        updatedPlayerMotion.setUpdatedAt(Instant.now());

        response.setPlayerMotion(Map.of(motionMessage.getActorId(), updatedPlayerMotion));

        return response;
    }

    private SocketResponse buildMonsterMotionSocketResponse(MotionMessage motionMessage) {
        SocketResponse response = new SocketResponse();
        response.setMessageType(SocketResponseType.MOB_MOTION_UPDATE.getType());

        Monster monster = new Monster();
        monster.setActorId(motionMessage.getActorId());
        monster.setActorId(motionMessage.getActorId());
        monster.setUpdatedAt(Instant.now());
        response.setMonsters(Map.of(motionMessage.getActorId(), monster));

        return response;
    }
}
