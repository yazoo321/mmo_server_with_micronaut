package server.monster.server_integration.service;

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
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Dead;
import server.attribute.status.service.StatusService;
import server.attribute.status.types.StatusTypes;
import server.combat.service.ActorThreatService;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.items.service.ItemService;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.repository.MobRepository;
import server.motion.dto.MotionResult;
import server.motion.repository.ActorMotionRepository;

@Slf4j
@Service
public class MobInstanceService {

    @Inject MobRepository mobRepository;

    @Inject
    ActorMotionRepository actorMotionRepository;

    @Inject StatsService statsService;

    @Inject StatusService statusService;

    @Inject
    ActorThreatService actorThreatService;

    @Inject
    ItemService itemService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Single<List<Monster>> getMobsNearby(Location location, int threshold) {
        return mobRepository.getMobsNearby(location, threshold);
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
        statusService.initializeStatus(mobId).subscribe();

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

    public void handleMobDeath(Stats mobStats) {
        String mobId = mobStats.getActorId();

        actorThreatService.resetActorThreat(mobId)
                .delaySubscription(2_000, TimeUnit.MILLISECONDS)
                .subscribe();

        actorMotionRepository.fetchActorMotion(mobId)
                .doOnError(err -> log.error(err.getMessage()))
                .doOnSuccess(motion -> itemService.handleItemDropsForMob(mobStats, motion)).subscribe();

        // we will set state to death and wait for animations etc
        statusService.removeAllStatuses(mobId)
                .doOnSuccess(statuses -> {
                    statusService.addStatusToActor(statuses, Set.of(new Dead()));
                    statusService
                            .deleteActorStatus(mobId)
                            .doOnError(err -> log.error(err.getMessage()))
                            .delaySubscription(2_000, TimeUnit.MILLISECONDS)
                            .subscribe();
                })
                .subscribe();


        statsService
                .deleteStatsFor(mobId)
                .doOnError(
                        err -> log.error("Failed to delete stats on death, {}", err.getMessage()))
                .delaySubscription(2_000, TimeUnit.MILLISECONDS)
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
                .delaySubscription(2_000, TimeUnit.MILLISECONDS)
                .doOnError(err -> log.error("error on handling mob death, {}", err.getMessage()))
                .subscribe();
    }
}
