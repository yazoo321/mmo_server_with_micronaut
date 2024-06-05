package server.motion.repository;

import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.scheduling.annotation.Scheduled;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
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

    private Map<String, Motion> motionMap = new ConcurrentHashMap<>();

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
        Motion prev = motionMap.put(actorId, motion);
        if (null == prev) {
            // sync it now if its fresh
            handleUpdate(actorId, motion);
        }
        return motion;
    }

    @Scheduled(fixedDelay = "30s", initialDelay = "1s")
    public void syncMotionWithRepo() {
        // we pull motion information from the cache and we update the cache as first resort
        // TODO: Convert to batch process

        for (String id : motionMap.keySet()) {
            Motion motion = motionMap.get(id);
            handleUpdate(id, motion);
            motionMap.remove(id);
        }
    }

    private void handleUpdate(String actorId, Motion motion) {
        if (UUIDHelper.isPlayer(actorId)) {
            playerMotionRepository
                    .updateMotion(new PlayerMotion(actorId, motion, null, null))
                    .doOnError(err -> log.error(err.getMessage()))
                    .subscribe();
        } else {
            mobRepository
                    .updateMotionOnly(actorId, motion)
                    .doOnError(err -> log.error(err.getMessage()))
                    .subscribe();
        }
    }
}
