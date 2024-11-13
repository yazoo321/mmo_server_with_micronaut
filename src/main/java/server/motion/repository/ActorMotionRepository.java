package server.motion.repository;

import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.scheduling.annotation.Scheduled;
import io.netty.util.internal.ConcurrentSet;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.common.uuid.UUIDHelper;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.repository.MobRepository;
import server.motion.dto.PlayerMotion;

@Slf4j
@Singleton
@CacheConfig("actor-motion-cache")
public class ActorMotionRepository {

    private static final String ACTOR_MOTION_CACHE = "actor-motion-cache";

    private Set<String> syncActorMotion = new ConcurrentSet<>();

    @Inject PlayerMotionRepository playerMotionRepository;

    @Inject MobRepository mobRepository;

    @Cacheable(value = ACTOR_MOTION_CACHE, parameters = "actorId")
    public Single<Motion> fetchActorMotion(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            return null;
        }

        if (UUIDHelper.isPlayer(actorId)) {
            return playerMotionRepository
                    .fetchPlayerMotion(actorId)
                    .map(PlayerMotion::getMotion)
                    .doOnError(err -> log.error(err.getMessage()));
        } else {
            return mobRepository
                    .findMobInstance(actorId)
                    .map(Monster::getMotion)
                    .doOnError(err -> log.error(err.getMessage()));
        }
    }

    @CachePut(value = ACTOR_MOTION_CACHE, parameters = "actorId", async = true)
    public Motion updateActorMotion(String actorId, Motion motion) {
        if (actorId == null) {
            log.error("actorId null when trying to update motion");
            return null;
        }
        if (!syncActorMotion.contains(actorId)) {
            // sync it now if its fresh
            handleUpdate(actorId, motion, true);
        }

        syncActorMotion.add(actorId);

        return motion;
    }

    @CacheInvalidate(value = ACTOR_MOTION_CACHE, parameters = "actorId")
    public void handleDisconnect(String actorId) {
        fetchActorMotion(actorId)
                .doOnSuccess(motion -> handleUpdate(actorId, motion, false))
                .doOnError(err -> log.error(err.getMessage()))
                .subscribe();
    }

    @Scheduled(fixedDelay = "1s", initialDelay = "1s")
    public void syncMotionWithRepo() {
        // we pull motion information from the cache and we update the cache as first resort
        // TODO: Convert to batch process
        log.info("Running syncMotionWithRepo scheduler");
        for (String id : syncActorMotion) {
            // fetch from cache:
            fetchActorMotion(id)
                    .doOnSuccess(motion -> handleUpdate(id, motion, true))
                    .doOnError(err -> log.error(err.getMessage()))
                    .subscribe();
        }
    }

    private void handleUpdate(String actorId, Motion motion, boolean online) {
        if (!online) {
            log.info("updating motion to be offline");
            log.info("{}, {}", actorId, motion);
        }
        syncActorMotion.remove(actorId);
        if (UUIDHelper.isPlayer(actorId)) {
            playerMotionRepository
                    .updateMotion(actorId, new PlayerMotion(actorId, motion, online, Instant.now()))
                    .doOnError(err -> log.error(err.getMessage()))
                    .doOnSuccess(done -> log.info("Updated! {}", done))
                    .subscribe();
        } else {
            mobRepository
                    .updateMotionOnly(actorId, motion)
                    .doOnError(err -> log.error(err.getMessage()))
                    .subscribe();
        }
    }

    public Single<List<String>> getNearbyPlayers(Location location, int threshold) {
        Motion motion = Motion.fromLocation(location);
        PlayerMotion fake = new PlayerMotion(null, motion, null, null);
        return playerMotionRepository.getPlayersNearby(fake, threshold)
                .map(playerMotions -> playerMotions.stream().map(PlayerMotion::getActorId).toList());
    }

    public Single<List<String>> getNearbyMobs(Location location, int threshold) {
        return mobRepository.getMobsNearby(location, threshold).map(m -> m.stream().map(Monster::getActorId).toList());
    }
}
