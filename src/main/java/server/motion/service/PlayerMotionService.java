package server.motion.service;

import com.mongodb.client.result.DeleteResult;
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
import server.motion.repository.ActorMotionRepository;
import server.motion.repository.PlayerMotionRepository;

@Slf4j
@Singleton
public class PlayerMotionService {

    @Inject PlayerMotionRepository playerMotionRepository;

    @Inject PlayerMotionUpdateProducer playerMotionUpdateProducer;

    @Inject
    ActorMotionRepository actorMotionRepository;

    private static final int DEFAULT_DISTANCE_THRESHOLD = 20_000;

    public static final Motion STARTING_MOTION =
            Motion.builder()
                    .map("tooksworth") // Set up default starting location to match your map
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
        log.info("Initializing actor motion: {}", actorId);
        PlayerMotion playerMotion = new PlayerMotion();
        playerMotion.setMotion(STARTING_MOTION);
        playerMotion.setActorId(actorId);
        playerMotion.setIsOnline(false);
        playerMotion.setUpdatedAt(Instant.now());

        return playerMotionRepository.insertPlayerMotion(actorId, playerMotion);
    }


    public Single<DeleteResult> deletePlayerMotion(String actorId) {
        log.info("Deleting player motion: {}", actorId);
        return playerMotionRepository.deletePlayerMotion(actorId);
    }

    public void disconnectPlayer(String actorId) {
        log.info("Disconnecting actor: {}", actorId);
        actorMotionRepository.handleDisconnect(actorId);
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
        log.info("Getting player motion: {}", actorIds);
        return playerMotionRepository.fetchPlayersMotion(actorIds);
    }

    public void relayPlayerMotion(PlayerMotion playerMotion) {
        log.info("relaying player motion: {}", playerMotion.getActorId());
        playerMotionUpdateProducer.sendPlayerMotionResult(playerMotion);
    }

    public void handlePlayerRespawn(String actorId, String customData) {

    }
}
