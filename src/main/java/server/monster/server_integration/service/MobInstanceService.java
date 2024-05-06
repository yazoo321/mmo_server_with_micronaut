package server.monster.server_integration.service;

import com.mongodb.client.result.InsertOneResult;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import server.attribute.stats.service.StatsService;
import server.attribute.status.model.derived.Dead;
import server.attribute.status.service.StatusService;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.repository.MobRepository;
import server.motion.dto.MotionResult;

@Slf4j
@Service
public class MobInstanceService {

    @Inject MobRepository mobRepository;

    @Inject StatsService statsService;

    @Inject StatusService statusService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Single<List<Monster>> getMobsNearby(Location location) {
        return mobRepository.getMobsNearby(location);
    }

    public Single<List<Monster>> getMobsByIds(Set<String> actorIds) {
        return mobRepository.getMobsByInstanceIds(actorIds);
    }

    public Single<Monster> createMob(String mobId, Motion motion) {
        Monster mob = new Monster();
        // TODO: choose whether we need mob ID or not
        mob.setActorId(mobId);
        mob.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        mob.setMotion(motion);

        statsService.initializeMobStats(mobId);
        statusService.initializeStatus(mobId);

        return mobRepository.insertMobInstance(mob);

        // also need to create mob attributes
    }

    public Single<Monster> createMob(Monster mob) {
        mob.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        return mobRepository.insertMobInstance(mob);
    }

    public Single<Monster> updateMobMotion(String actorId, Motion motion) {
        return mobRepository.updateMotionOnly(actorId, motion);
    }

    public MotionResult buildMobMotionResult(String actorId, Motion motion) {
        Monster monster = new Monster();
        monster.setActorId(actorId);
        monster.setMotion(motion);
        // we don't populate other info here

        return MotionResult.builder().monster(monster).build();
    }

    public void handleMobDeath(String mobId) {
        // we will set state to death and wait for animations etc

        statusService.addStatusToActor(Set.of(new Dead()), mobId);
        statusService
                .deleteActorStatus(mobId)
                .doOnError(err -> log.error(err.getMessage()))
                .delaySubscription(10_000, TimeUnit.MILLISECONDS)
                .subscribe();

        statsService
                .deleteStatsFor(mobId)
                .doOnError(
                        err -> log.error("Failed to delete stats on death, {}", err.getMessage()))
                .delaySubscription(10_000, TimeUnit.MILLISECONDS)
                .subscribe();

        Single.fromCallable(
                        () ->
                                mobRepository
                                        .deleteMobInstance(mobId)
                                        .doOnError(
                                                err ->
                                                        log.error(
                                                                "Failed to delete mob instance, {}",
                                                                err.getMessage()))
                                        .subscribe())
                .delaySubscription(10_000, TimeUnit.MILLISECONDS)
                .doOnError(err -> log.error("error on handling mob death, {}", err.getMessage()))
                .subscribe();
    }
}
