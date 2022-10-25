package server.player.motion.socket.v1.service;

import lombok.extern.slf4j.Slf4j;
import server.common.dto.Motion;
import server.player.motion.dto.PlayerMotion;
import server.player.motion.socket.v1.model.PlayerMotionList;
import server.player.motion.socket.v1.repository.PlayerMotionRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.List;

@Slf4j
@Singleton
public class PlayerMotionService {

    @Inject
    PlayerMotionRepository playerMotionRepository;

    public static final Motion STARTING_MOTION = Motion.builder()
            .map("MAP1")
            .vx(0)
            .vy(0)
            .vz(0)
            .x(0)
            .y(0)
            .z(0)
            .isFalling(false)
            .pitch(0)
            .roll(0)
            .yaw(0)
            .build();

    public PlayerMotion initializePlayerMotion(String playerName) {
        // can create custom start points for different classes/maps etc
        PlayerMotion playerMotion = new PlayerMotion();
        playerMotion.setMotion(STARTING_MOTION);
        playerMotion.setPlayerName(playerName);
        playerMotion.setIsOnline(false);
        playerMotion.setUpdatedAt(Instant.now());

        playerMotionRepository.insertPlayerMotion(playerMotion);

        return playerMotion;
    }

    public void deletePlayerMotion(String playerName) {

    }

    public PlayerMotionList updatePlayerMotion(String playerName, Motion motion) {
        PlayerMotion playerMotion = new PlayerMotion(playerName, motion, true, Instant.now());
        playerMotionRepository.updatePlayerMotion(playerMotion);

        return getPlayersNearMe(motion, playerName);
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
