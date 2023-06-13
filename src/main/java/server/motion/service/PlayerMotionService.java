package server.motion.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Motion;
import server.motion.dto.PlayerMotion;
import server.motion.model.PlayerMotionList;
import server.motion.producer.PlayerMotionUpdateProducer;
import server.motion.repository.PlayerMotionRepository;

@Slf4j
@Singleton
public class PlayerMotionService {

    @Inject PlayerMotionRepository playerMotionRepository;

    @Inject PlayerMotionUpdateProducer playerMotionUpdateProducer;

    private static int DEFAULT_DISTANCE_THRESHOLD = 1000;

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

    public Single<PlayerMotion> initializePlayerMotion(String playerName) {
        // can create custom start points for different classes/maps etc
        PlayerMotion playerMotion = new PlayerMotion();
        playerMotion.setMotion(STARTING_MOTION);
        playerMotion.setPlayerName(playerName);
        playerMotion.setIsOnline(false);
        playerMotion.setUpdatedAt(Instant.now());

        return playerMotionRepository.insertPlayerMotion(playerMotion);
    }

    public void deletePlayerMotion(String playerName) {
        playerMotionRepository.deletePlayerMotion(playerName);
    }

    public PlayerMotion buildPlayerMotion(String playerName, String map, Motion motion) {
        motion.setMap(map);
        return new PlayerMotion(playerName, motion, true, Instant.now());
    }

    public Single<PlayerMotion> updatePlayerMotion(PlayerMotion playerMotion) {
        return playerMotionRepository.updateMotion(playerMotion);
    }

    // used in v1
    public Single<PlayerMotion> updatePlayerMotion(String playerName, Motion motion) {
        PlayerMotion playerMotion = new PlayerMotion(playerName, motion, true, Instant.now());
        return playerMotionRepository.updateMotion(playerMotion);
    }

    public void disconnectPlayer(String playerName) {
        PlayerMotion motion = playerMotionRepository.findPlayerMotion(playerName).blockingGet();

        motion.setIsOnline(false);

        playerMotionRepository.updateMotion(motion);
    }

    public Single<PlayerMotionList> getPlayersNearMe(Motion motion, String playerName) {
        PlayerMotion playerMotion = new PlayerMotion(playerName, motion, true, Instant.now());
        return playerMotionRepository
                .getPlayersNearby(playerMotion, DEFAULT_DISTANCE_THRESHOLD)
                .doOnError(e -> log.error("Failed to get players motion, {}", e.getMessage()))
                .map(PlayerMotionList::new);
    }

    public Single<List<PlayerMotion>> getNearbyPlayersAsync(
            Motion motion, String playerName, Integer threshold) {
        PlayerMotion playerMotion = new PlayerMotion(playerName, motion, true, Instant.now());
        threshold = threshold == null ? DEFAULT_DISTANCE_THRESHOLD : threshold;

        return playerMotionRepository.getPlayersNearby(playerMotion, threshold);
    }

    public Single<PlayerMotion> getPlayerMotion(String playerName) {
        return playerMotionRepository.findPlayerMotion(playerName);
    }

    public Single<List<PlayerMotion>> getPlayersMotion(Set<String> playerNames) {
        return playerMotionRepository.findPlayersMotion(playerNames);
    }

    public void relayPlayerMotion(PlayerMotion playerMotion) {
        playerMotionUpdateProducer.sendPlayerMotionResult(playerMotion);
    }
}
