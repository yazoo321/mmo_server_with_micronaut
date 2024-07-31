package server.attribute.stats.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static final Set<String> AVAILABLE_CLASSES =
            Set.of(
                    ClassTypes.MAGE.getType(),
                    ClassTypes.FIGHTER.getType(),
                    ClassTypes.RANGER.getType(),
                    ClassTypes.CLERIC.getType());

    public static final Set<String> AVAILABLE_BASE_STAT =
            Set.of(
                    StatsTypes.STR.getType(),
                    StatsTypes.STA.getType(),
                    StatsTypes.INT.getType(),
                    StatsTypes.DEX.getType());


    public Single<Stats> initializeCharacterClass(String actorId, String playerClass) {
        if (!isClassValid(playerClass)) {
            throw new CharacterException("Invalid class selected");
        }

        Stats stats = statsService.getStatsFor(actorId).blockingGet();
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
        return statsRepository.updateStats(stats.getActorId(), stats);
    }

    public Single<Stats> handleLevelUp(String actorId, String classToLevel) {
        // TODO: additional validation, check XP
        if (!AVAILABLE_CLASSES.contains(classToLevel)) {
            throw new RuntimeException("Failed to level-up, bad request");
        }

        return statsService
                .getStatsFor(actorId)
                .doOnSuccess(
                        stats -> {
                            int currentXp = stats.getBaseStat(StatsTypes.XP);
                            int currentLevel = stats.getBaseStat(StatsTypes.LEVEL);
                            int xpRequired = xpRequiredForLevel(currentLevel);

                            if (currentXp < xpRequired) {
                                log.error("Tried to level but not got enough XP!, {}", actorId);
                                return;
                            }

                            int availablePts = stats.addToBase(StatsTypes.AVAILABLE_PTS, 5);
                            int newLevel = stats.addToBase(classToLevel, 1);
                            int baseLevel = stats.addToBase(StatsTypes.LEVEL, 1);

                            int canLevel = currentXp > xpRequiredForLevel(baseLevel) ? 1 : 0;
                            stats.setBase(StatsTypes.CAN_LEVEL, canLevel);

                            Map<String, Integer> baseChange =
                                    Map.of(
                                            classToLevel,
                                            newLevel,
                                            StatsTypes.AVAILABLE_PTS.getType(),
                                            availablePts,
                                            StatsTypes.LEVEL.getType(),
                                            baseLevel,
                                            StatsTypes.CAN_LEVEL.getType(),
                                            canLevel);
                            Map<String, Double> derivedChanged = stats.recalculateDerivedStats();

                            statsService.handleBaseDifference(baseChange, stats);
                            statsService.handleDifference(derivedChanged, stats);
                        })
                .doOnError(
                        err -> log.error("Failed to get stats on level up, {}", err.getMessage()));
    }

    private Single<Stats> handleAddStatPoint(String actorId, String stat) {
        if (!isBaseStatValid(stat)) {
            log.error("Tried adding invalid {} stat, {}", stat, actorId);
        }

        return statsService
                .getStatsFor(actorId)
                .doOnSuccess(
                        stats -> {
                            int availablePoints = stats.addToBase(StatsTypes.AVAILABLE_PTS, -1);
                            if (availablePoints < 0) {
                                return;
                            }

                            int updated = stats.addToBase(stat, 1);
                            Map<String, Integer> baseChange = Map.of(
                                    stat, updated,
                                    StatsTypes.AVAILABLE_PTS.getType(), availablePoints);
                            Map<String, Double> derivedChanged = stats.recalculateDerivedStats();

                            statsService.handleBaseDifference(baseChange, stats);
                            statsService.handleDifference(derivedChanged, stats);
                        })
                .doOnError(e-> log.error(e.getMessage()));
    }

    public void handleAddXp(Stats targetStats, Stats actorStats) {
        if (!actorStats.isPlayer()) {
            return;
        }
        int xpToAdd = evalXpToAdd(targetStats, actorStats);

        addPlayerXp(actorStats, xpToAdd);
    }

    public Single<Stats> addPlayerXp(String actorId, Integer xpToAdd) {
        if (xpToAdd == null || xpToAdd < 1) {
            throw new IllegalArgumentException("Bad request to add player XP");
        }

        return statsService
                .getStatsFor(actorId)
                .doOnSuccess(stats -> addPlayerXp(stats, xpToAdd))
                .doOnError(err -> log.error("Failed to add XP, {}", err.getMessage()));
    }

    public int evalXpToAdd(Stats targetStats, Stats actorStats) {
        int deadActorLevel = targetStats.getBaseStat(StatsTypes.LEVEL);
        int levelDiff = deadActorLevel - actorStats.getBaseStat(StatsTypes.LEVEL);

        int xpPerLevel = 100;

        float multiplier = (float) ((levelDiff / 10) + 1);

        return Float.valueOf(deadActorLevel * xpPerLevel * multiplier).intValue();
    }

    public void addPlayerXp(Stats playerStats, Integer xpToAdd) {
        int newXp = playerStats.addToBase(StatsTypes.XP, xpToAdd);
        Map<String, Integer> updated = new HashMap<>(Map.of(StatsTypes.XP.getType(), newXp));

        int canLevel = playerStats.getBaseStat(StatsTypes.CAN_LEVEL);
        if (canLevel == 0) {
            if (newXp >= xpRequiredForLevel(playerStats.getBaseStat(StatsTypes.LEVEL))) {
                updated.put(StatsTypes.CAN_LEVEL.getType(), 1);
                playerStats.setBase(StatsTypes.CAN_LEVEL, 1);
            }
        }

        statsService.handleBaseDifference(updated, playerStats);
        //        return statsService.update(playerStats);
    }

    public void handleAddBaseStat(String actorId, String statType) {
        if (isClassValid(statType)) {
            handleLevelUp(actorId, statType).subscribe();
            return;
        }

        if (isBaseStatValid(statType)) {
            handleAddStatPoint(actorId, statType).subscribe();
            return;
        }

        log.error("Erroneous stat type being added: {} on actor: {}", statType, actorId);
    }

    private boolean isClassValid(String className) {
        // consider other validations

        return AVAILABLE_CLASSES.contains(className);
    }

    private boolean isBaseStatValid(String statType) {
        return AVAILABLE_BASE_STAT.contains(statType);
    }

    private int xpRequiredForLevel(int level) {
        //      TODO: make better equations for level and xp
        return 5000 * level;
    }
}
