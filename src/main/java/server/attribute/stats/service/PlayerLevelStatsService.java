package server.attribute.stats.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.repository.ActorStatsRepository;
import server.attribute.stats.types.StatsTypes;
import server.player.exceptions.CharacterException;

@Singleton
@Slf4j
public class PlayerLevelStatsService {

    @Inject StatsService statsService;

    @Inject ActorStatsRepository statsRepository;

    public static final List<String> AVAILABLE_CLASSES =
            List.of(
                    ClassTypes.MAGE.getType(),
                    ClassTypes.FIGHTER.getType(),
                    ClassTypes.CLERIC.getType());

    public Single<Stats> initializeCharacterClass(String playerName, String playerClass) {
        if (!isClassValid(playerClass)) {
            throw new CharacterException("Invalid class selected");
        }

        Stats stats = statsService.getStatsFor(playerName).blockingGet();
        Map<String, Integer> baseAttr = stats.getBaseStats();

        Map<String, Integer> toAdd =
                new HashMap<>(
                        Map.of(
                                StatsTypes.LEVEL.type, 1,
                                StatsTypes.XP.type, 0));
        baseAttr.putAll(toAdd);

        AVAILABLE_CLASSES.forEach(
                c -> {
                    Integer level = c.equalsIgnoreCase(playerClass) ? 1 : 0;
                    baseAttr.put(c, level);
                });

        stats.recalculateDerivedStats();
        return statsRepository.updateStats(stats);
    }

    public Single<Stats> handleLevelUp(String playerName, String classToLevel) {
        // TODO: additional validation, check XP
        if (!AVAILABLE_CLASSES.contains(classToLevel)) {
            throw new RuntimeException("Failed to level-up, bad request");
        }

        return statsService
                .getStatsFor(playerName)
                .flatMap(
                        stats -> {
                            Map<String, Integer> baseAttr = stats.getBaseStats();
                            baseAttr.put(classToLevel, baseAttr.get(classToLevel) + 1);
                            stats.recalculateDerivedStats();
                            return statsRepository.updateStats(stats);
                        })
                .doOnError(
                        err -> log.error("Failed to get stats on level up, {}", err.getMessage()));
    }

    public Single<Stats> addPlayerXp(String playerName, Integer xpToAdd) {
        if (xpToAdd < 1) {
            throw new IllegalArgumentException("Bad request to add player XP");
        }

        return statsService
                .getStatsFor(playerName)
                .doOnSuccess(
                        stats -> {
                            Map<String, Double> attr = stats.getDerivedStats();
                            attr.put(
                                    StatsTypes.XP.type,
                                    attr.getOrDefault(StatsTypes.XP.type, 0.0) + xpToAdd);
                            Map<String, Double> dataToSend =
                                    Map.of(StatsTypes.XP.type, attr.get(StatsTypes.XP.type));
                            statsService.handleDifference(dataToSend, stats);
                        })
                .doOnError(err -> log.error("Failed to add XP, {}", err.getMessage()));
    }

    private boolean isClassValid(String className) {
        // consider other validations

        return AVAILABLE_CLASSES.contains(className);
    }
}
