package server.attribute.stats.service;

import com.mongodb.client.result.DeleteResult;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.repository.ActorStatsRepository;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.monster.server_integration.service.MobInstanceService;
import server.socket.producer.UpdateProducer;

@Slf4j
@Singleton
public class StatsService {

    @Inject ActorStatsRepository repository;

    @Inject UpdateProducer updateProducer;

    public void initializeMobStats(String actorId) {
        Stats mobStats = new Stats();

        mobStats.setActorId(actorId);

        mobStats.setBaseStats(
                new HashMap<>(
                        Map.of(
                                StatsTypes.STR.getType(), 200,
                                StatsTypes.STA.getType(), 100,
                                StatsTypes.DEX.getType(), 100,
                                StatsTypes.INT.getType(), 100)));

        mobStats.getDerivedStats()
                .putAll(
                        new HashMap<>(
                                Map.of(
                                        StatsTypes.CURRENT_HP.getType(), 100.0,
                                        StatsTypes.CURRENT_MP.getType(), 50.0)));

        mobStats.recalculateDerivedStats();
        mobStats.setDerived(StatsTypes.CURRENT_HP, mobStats.getDerived(StatsTypes.MAX_HP));
        mobStats.setAttributePoints(0);

        repository.updateStats(mobStats).subscribe();
    }

    public void initializePlayerStats(String playerName) {
        Stats playerStats = new Stats();

        playerStats.setActorId(playerName);

        playerStats.setBaseStats(
                new HashMap<>(
                        Map.of(
                                StatsTypes.STR.getType(), 15,
                                StatsTypes.STA.getType(), 15,
                                StatsTypes.DEX.getType(), 15,
                                StatsTypes.INT.getType(), 15)));

        playerStats
                .getDerivedStats()
                .putAll(
                        new HashMap<>(
                                Map.of(
                                        StatsTypes.CURRENT_HP.getType(), 100.0,
                                        StatsTypes.CURRENT_MP.getType(), 50.0)));

        playerStats.recalculateDerivedStats();

        playerStats.setAttributePoints(0);

        repository.updateStats(playerStats).subscribe();
    }

    public Single<Stats> getStatsFor(String actorId) {
        return repository.findActorStats(actorId);
    }

    public Single<DeleteResult> deleteStatsFor(String actorId) {
        return repository.deleteAttributes(actorId);
    }

    public void updateItemStats(String playerName, Map<String, Double> itemStats) {
        repository
                .findActorStats(playerName)
                .doOnSuccess(
                        stats -> {
                            stats.setItemEffects(itemStats);
                            Map<String, Double> updated = stats.recalculateDerivedStats();
                            handleDifference(updated, stats);
                        })
                .subscribe();
    }

    public void takeDamage(String actorId, Double damage) {
        getStatsFor(actorId)
                .doOnSuccess(
                        stats -> {
                            Map<String, Double> derived = stats.getDerivedStats();
                            Double newHp = derived.get(StatsTypes.CURRENT_HP.getType()) - damage;
                            Map<String, Double> updated =
                                    Map.of(StatsTypes.CURRENT_HP.getType(), newHp);
                            derived.put(StatsTypes.CURRENT_HP.getType(), newHp);
                            handleDifference(updated, stats);
                        })
                .subscribe();
    }

    public Stats takeDamage(Stats stats, Map<DamageTypes, Double> damageMap) {
        Map<String, Double> derived = stats.getDerivedStats();
        damageMap.forEach(
                (k, v) -> {
                    Double currentHp = derived.get(StatsTypes.CURRENT_HP.getType());
                    currentHp -= v;
                    derived.put(StatsTypes.CURRENT_HP.getType(), currentHp);
                    Map<String, Double> updated =
                            Map.of(StatsTypes.CURRENT_HP.getType(), currentHp);
                    handleDifference(updated, stats);
                });

        return stats;
    }

    private void handleDifference(Map<String, Double> updated, Stats stats) {
        if (!updated.isEmpty()) {
            repository.updateStats(stats).subscribe();
            Stats notifyUpdates =
                    Stats.builder().actorId(stats.getActorId()).derivedStats(updated).build();
            updateProducer.updateStats(notifyUpdates);
        }
    }
}
