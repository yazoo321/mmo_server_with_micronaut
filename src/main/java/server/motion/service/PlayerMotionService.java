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

    //    @Inject SessionParamHelper sessionParamHelper;

    private static final int DEFAULT_DISTANCE_THRESHOLD = 1000;

    public static final Motion STARTING_MOTION =
            Motion.builder()
                    .map("Tooksworth") // Set up default starting location to match your map
                    .x(240)
                    .y(350)
                    .z(230)
                    .vx(0)
                    .vy(0)
                    .vz(0)
                    .isFalling(false)
                    .pitch(0)
                    .roll(0)
                    .yaw(0)
                    .build();

    public Single<Motion> initializePlayerMotion(String actorId) {
        // can create custom start points for different classes/maps etc
        PlayerMotion playerMotion = new PlayerMotion();
        playerMotion.setMotion(STARTING_MOTION);
        playerMotion.setActorId(actorId);
        playerMotion.setIsOnline(false);
        playerMotion.setUpdatedAt(Instant.now());

        return playerMotionRepository.insertPlayerMotion(actorId, playerMotion);
    }

    public void deletePlayerMotion(String actorId) {
        playerMotionRepository.deletePlayerMotion(actorId).subscribe();
    }

    // used in v1
    public Single<PlayerMotion> updatePlayerMotion(String actorId, Motion motion) {
        PlayerMotion playerMotion = new PlayerMotion(actorId, motion, true, Instant.now());
        return playerMotionRepository.updateMotion(playerMotion);
    }

    public void disconnectPlayer(String actorId) {
        playerMotionRepository.setPlayerOnlineStatus(actorId, false).subscribe();
    }

    @Deprecated
    public Single<PlayerMotionList> getPlayersNearMe(Motion motion, String actorId) {
        PlayerMotion playerMotion = new PlayerMotion(actorId, motion, true, Instant.now());
        return playerMotionRepository
                .getPlayersNearby(playerMotion, DEFAULT_DISTANCE_THRESHOLD)
                .doOnError(e -> log.error("Failed to get players motion, {}", e.getMessage()))
                .map(PlayerMotionList::new);
    }

    public Single<List<PlayerMotion>> getNearbyPlayersAsync(
            Motion motion, String actorId, Integer threshold) {
        PlayerMotion playerMotion = new PlayerMotion(actorId, motion, true, Instant.now());
        threshold = threshold == null ? DEFAULT_DISTANCE_THRESHOLD : threshold;

        return playerMotionRepository.getPlayersNearby(playerMotion, threshold);
    }

    public Single<PlayerMotion> getPlayerMotion(String actorId) {
        return playerMotionRepository.fetchPlayerMotion(actorId);
    }

    public Single<List<PlayerMotion>> getPlayersMotion(Set<String> actorIds) {
        return playerMotionRepository.fetchPlayersMotion(actorIds);
    }

    public void relayPlayerMotion(PlayerMotion playerMotion) {
        playerMotionUpdateProducer.sendPlayerMotionResult(playerMotion);
    }
}
