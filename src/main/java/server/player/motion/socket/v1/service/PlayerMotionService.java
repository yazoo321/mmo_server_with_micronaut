package server.player.motion.socket.v1.service;

import io.micronaut.websocket.WebSocketBroadcaster;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Motion;
import server.player.motion.dto.PlayerMotion;
import server.player.motion.socket.v1.model.PlayerMotionList;
import server.player.motion.socket.v1.repository.PlayerMotionRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Singleton
public class PlayerMotionService {

    @Inject
    PlayerMotionRepository playerMotionRepository;

    public PlayerMotion initializePlayerMotion(String playerName) {
        // can create custom start points for different classes/maps etc
        PlayerMotion playerMotion = new PlayerMotion();
        Motion startMotion = new Motion();
        startMotion.setX(0);
        startMotion.setY(0);
        startMotion.setZ(0);

        startMotion.setVx(0);
        startMotion.setVy(0);
        startMotion.setVz(0);

        startMotion.setPitch(0);
        startMotion.setRoll(0);
        startMotion.setYaw(0);

        startMotion.setIsFalling(false);

        playerMotion.setMotion(startMotion);
        playerMotion.setPlayerName(playerName);

        playerMotionRepository.insertPlayerMotion(playerMotion);

        return playerMotion;
    }

    public void updatePlayerMotion(String playerName, Motion motion) {
//        if (!Objects.equals(playerName, motion.getPlayerName())) {
//            log.error("Logged in player {} tried to update motion for player {}", playerName, motion.getPlayerName());
//            throw new PlayerMotionException("Tried to update motion for incorrect player name");
//        }

        PlayerMotion playerMotion = new PlayerMotion(playerName, motion, true, Instant.now());
        playerMotionRepository.updatePlayerMotion(playerMotion);
    }

    public void disconnectPlayer(String playerName) {
        PlayerMotion motion = playerMotionRepository.findPlayerMotion(playerName);

        motion.setIsOnline(false);

        playerMotionRepository.updatePlayerMotion(motion);
    }

    public PlayerMotionList getPlayersNearMe(Motion motion, String playerName) {
        PlayerMotion playerMotion = new PlayerMotion(playerName, motion, true, Instant.now());
        List<PlayerMotion> playerMotions = playerMotionRepository.getPlayersNearby(playerMotion);

        return new PlayerMotionList(playerMotions);
    }

}
