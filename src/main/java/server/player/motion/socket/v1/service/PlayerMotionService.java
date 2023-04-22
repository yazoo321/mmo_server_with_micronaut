package server.player.motion.socket.v1.service;

import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import server.common.dto.Motion;
import server.player.motion.dto.PlayerMotion;
import server.player.motion.socket.v1.model.PlayerMotionList;
import server.player.motion.socket.v1.repository.PlayerMotionRepository;

@Slf4j
@Service
public class PlayerMotionService {

    @Inject PlayerMotionRepository playerMotionRepository;

    public static final Motion STARTING_MOTION =
            Motion.builder()
                    .map("dreamscape") // Set up default starting location to match your map
                    .x(34723)
                    .y(-69026)
                    .z(-20121)
                    .vx(0)
                    .vy(0)
                    .vz(0)
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
        playerMotionRepository.deletePlayerMotion(playerName);
    }

    public void updatePlayerMotion(String playerName, Motion motion) {
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

    public PlayerMotion getPlayerMotion(String playerName) {
        return playerMotionRepository.findPlayerMotion(playerName);
    }
}
