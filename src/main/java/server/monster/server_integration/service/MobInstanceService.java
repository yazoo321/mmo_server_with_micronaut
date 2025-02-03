package server.monster.server_integration.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.status.service.StatusService;
import server.combat.service.ActorThreatService;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.items.service.ItemService;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.repository.MobRepository;
import server.motion.dto.MotionResult;
import server.motion.repository.ActorMotionRepository;
import server.socket.producer.UpdateProducer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MobInstanceService {

    @Inject MobRepository mobRepository;

    @Inject ActorMotionRepository actorMotionRepository;

    @Inject StatsService statsService;

    @Inject StatusService statusService;

    @Inject ActorThreatService actorThreatService;

    @Inject ItemService itemService;

    @Inject UpdateProducer updateProducer;

    private final int DEATH_DELETE_TIME = 10_000;

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

        actorMotionRepository
                .fetchActorMotion(mobId)
                .doOnError(err -> log.error(err.getMessage()))
                .doOnSuccess(motion -> itemService.handleItemDropsForMob(mobStats, motion))
                .subscribe();


        log.info("Will delete mob, current timestamp: {}", Instant.now());
        Single.fromCallable(() -> mobRepository
                        .deleteMobInstance(mobId)
                        .doOnError(err -> log.error(
                                "Failed to delete mob instance, {}",
                                err.getMessage()))
                        .subscribe())
                .delaySubscription(10_000, TimeUnit.MILLISECONDS)
                .doOnError(err -> log.error("error on handling mob death, {}", err.getMessage()))
                .subscribe();

        Single.fromCallable(
                        () -> {
                            updateProducer.removeMobsFromGame(mobId);
                            return 1;
                        })
                .delaySubscription((DEATH_DELETE_TIME - 50), TimeUnit.MILLISECONDS)
                .subscribe();

        actorThreatService
                .resetActorThreat(mobId)
                .delaySubscription(DEATH_DELETE_TIME, TimeUnit.MILLISECONDS)
                .subscribe();
    }
}
