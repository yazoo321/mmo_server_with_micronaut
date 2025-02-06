package server.attribute.stats.service;

import com.mongodb.client.result.DeleteResult;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeApplyType;
import server.attribute.stats.model.DamageSource;
import server.attribute.stats.model.DamageUpdateMessage;
import server.attribute.stats.model.Stats;
import server.attribute.stats.repository.ActorStatsRepository;
import server.attribute.stats.types.DamageAdditionalData;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.talents.service.TalentService;
import server.combat.model.CombatData;
import server.combat.repository.CombatDataCache;
import server.combat.service.ActorThreatService;
import server.common.uuid.UUIDHelper;
import server.session.SessionParamHelper;
import server.socket.producer.UpdateProducer;

@Slf4j
@Singleton
public class StatsService {
    // TODO: Service getting quite large, consider splitting

    @Inject ActorStatsRepository repository;

    @Inject UpdateProducer updateProducer;

    @Inject SessionParamHelper sessionParamHelper;

    @Inject ActorThreatService threatService;

    @Inject TalentService talentService;

    @Inject CombatDataCache combatDataCache;

    private final Random rand = new Random();

    public static final Set<String> PHYSICAL_ATTACK_TYPES =
            Set.of(
                    DamageTypes.PHYSICAL.getType(),
                    DamageTypes.BLUDGEONING.getType(),
                    DamageTypes.PIERCING.getType(),
                    DamageTypes.SLASHING.getType(),
                    DamageTypes.BLEEDING.getType());

    public void initializeMobStats(String actorId) {
        Stats mobStats = new Stats();
        // TODO: parameterize from server
        int level = 10;
        mobStats.setActorId(actorId);

        mobStats.setBaseStats(
                new HashMap<>(
                        Map.of(
                                StatsTypes.STR.getType(), 100,
                                StatsTypes.STA.getType(), 100,
                                StatsTypes.DEX.getType(), 100,
                                StatsTypes.INT.getType(), 100)));

        mobStats.getDerivedStats()
                .putAll(
                        new HashMap<>(
                                Map.of(
                                        StatsTypes.CURRENT_HP.getType(), 100.0,
                                        StatsTypes.CURRENT_MP.getType(), 50.0)));

        mobStats.setBase(StatsTypes.LEVEL, level);

        mobStats.recalculateDerivedStats();
        mobStats.setDerived(StatsTypes.CURRENT_HP, mobStats.getDerived(StatsTypes.MAX_HP));
        mobStats.setAttributePoints(0);

        repository.updateStats(mobStats.getActorId(), mobStats).blockingSubscribe();
        CombatData combatData = new CombatData(actorId);
        combatDataCache.cacheCombatData(actorId, combatData);
    }

    public Single<Stats> initializePlayerStats(String actorId) {
        Stats playerStats = new Stats();

        playerStats.setActorId(actorId);

        playerStats.setBaseStats(
                new HashMap<>(
                        Map.of(
                                StatsTypes.STR.getType(), 15,
                                StatsTypes.STA.getType(), 15,
                                StatsTypes.DEX.getType(), 15,
                                StatsTypes.INT.getType(), 15,
                                StatsTypes.AVAILABLE_PTS.getType(), 0)));

        playerStats
                .getDerivedStats()
                .putAll(
                        new HashMap<>(
                                Map.of(
                                        StatsTypes.CURRENT_HP.getType(), 200.0,
                                        StatsTypes.CURRENT_MP.getType(), 50.0)));

        playerStats.recalculateDerivedStats();

        playerStats.setAttributePoints(0);

        return repository.updateStats(playerStats.getActorId(), playerStats);
    }

    public Single<Stats> update(Stats stats) {
        return repository.updateStats(stats.getActorId(), stats);
    }

    public Single<Stats> getStatsFor(String actorId) {
        return repository
                .fetchActorStats(actorId)
                .doOnError(
                        err ->
                                log.error(
                                        "Failed to get stats for {}, {}",
                                        actorId,
                                        err.getMessage()));
    }

    public Single<DeleteResult> deleteStatsFor(String actorId) {
        return repository.deleteStats(actorId);
    }

    public void updateItemStats(String actorId, Map<String, Double> itemStats) {
        repository
                .fetchActorStats(actorId)
                .doOnSuccess(
                        stats -> {
                            stats.setItemEffects(itemStats);
                            Map<String, Double> updated = stats.recalculateDerivedStats();
                            handleDifference(updated, stats);
                        })
                .doOnError(err -> log.error("Failed to update item stats, {}", err.getMessage()))
                .blockingSubscribe();
    }

    public void resetHPAndMP(String actorId, Double hpPercent, Double mpPercent) {
        getStatsFor(actorId)
                .doOnSuccess(
                        stats -> {
                            Map<String, Double> updated = stats.recalculateDerivedStats();

                            Double updatedHp = stats.getDerived(StatsTypes.MAX_HP) * hpPercent;
                            Double updatedMp = stats.getDerived(StatsTypes.MAX_MP) * mpPercent;

                            stats.setDerived(StatsTypes.CURRENT_HP, updatedHp);
                            stats.setDerived(StatsTypes.CURRENT_MP, updatedMp);

                            updated.put(StatsTypes.CURRENT_HP.getType(), updatedHp);
                            updated.put(StatsTypes.CURRENT_MP.getType(), updatedMp);

                            handleDifference(updated, stats);
                        })
                .doOnError(err -> log.error(err.getMessage()))
                .subscribe();
    }

    public void flatHP_MP_Mod(DamageSource damageSource) {
        // flat damage will bypass any damage reduction, threats, etc
        // for HP, should only be used for heal, as will bypass death checks

        if (damageSource.getAdditionalData().equals("PERCENT")) {
            // this is used within talents to restore HP/Mana for example on kill

            getStatsFor(damageSource.getActorId())
                    .doOnSuccess(
                            actorStats -> {
                                if (damageSource.getDamageMap().containsKey("HP")) {
                                    double hpVal = damageSource.getDamageMap().get("HP");
                                    double maxHp = actorStats.getDerived(StatsTypes.MAX_HP);

                                    hpVal = maxHp * hpVal;

                                    addHealth(actorStats, hpVal);
                                }

                                if (damageSource.getDamageMap().containsKey("MP")) {
                                    double mpVal = damageSource.getDamageMap().get("MP");
                                    double maxMp = actorStats.getDerived(StatsTypes.MAX_MP);

                                    mpVal = maxMp * mpVal;

                                    addMana(actorStats, mpVal);
                                }
                            })
                    .subscribe();
        }
    }

    public void takeDamage(String actorId, Map<String, Double> damageMap, String sourceActorId) {
        Single<Stats> targetActor = getStatsFor(actorId);
        Single<Stats> sourceActor = getStatsFor(sourceActorId);

        Single.zip(
                        targetActor,
                        sourceActor,
                        (targetStats, sourceStats) -> {
                            return takeDamage(targetStats, damageMap, sourceStats);
                        })
                .subscribe();
    }

    private void handleTalentApplyOnApplyType(
            Stats sourceActor, Stats targetStats, AttributeApplyType type) {
        talentService
                .getActorTalentsOfApplyType(sourceActor.getActorId(), type.getType())
                .doOnSuccess(
                        talentMap ->
                                talentMap.forEach(
                                        (talent, level) ->
                                                talent.applyEffect(
                                                        level,
                                                        talentService,
                                                        sourceActor,
                                                        targetStats)))
                .subscribe();
    }

    private boolean handleDodge(Map<String, Double> damageMap, Stats stats, Stats sourceStats) {
        if (damageMap.containsKey(DamageTypes.PHYSICAL.getType())) {
            // this is a physical attack, check dodge chance
            double dodgeChance = stats.getDerived(StatsTypes.DODGE);
            double chance = rand.nextDouble(100.0);

            if (chance <= dodgeChance) {
                // dodged the attack
                DamageSource damageSource =
                        DamageSource.builder()
                                .sourceActorId(sourceStats.getActorId())
                                .actorId(stats.getActorId())
                                .additionalData(DamageAdditionalData.DODGE.getType())
                                .damageMap(new HashMap<>())
                                .build();
                updateProducer.updateDamage(
                        new DamageUpdateMessage(damageSource, stats, sourceStats));

                handleTalentApplyOnApplyType(sourceStats, stats, AttributeApplyType.ON_DODGE_APPLY);
                handleTalentApplyOnApplyType(
                        stats, sourceStats, AttributeApplyType.ON_DODGE_CONSUME);
                return true;
            }
        }
        return false;
    }

    private boolean handleCrit(Map<String, Double> damageMap, Stats stats, Stats sourceStats) {
        // TODO: specific checks for magic / physical crit chances
        // e.g. physical, piercing, slashing, bleeding are all physical
        // magical, frost, fire, etc are magic damage
        // points to consider, what about imbued ? physical damage with magical elements
        double phyCritRoll = stats.getDerived(StatsTypes.PHY_CRIT);
        double mgcCritRoll = stats.getDerived(StatsTypes.MGC_CRIT);

        double phyChance = rand.nextDouble(100.0);
        double mgcChance = rand.nextDouble(100.0);

        boolean phyCrit = phyChance <= phyCritRoll;
        boolean mgcCrit = mgcChance <= mgcCritRoll;
        if (!(phyCrit || mgcCrit)) {
            return false;
        }

        // critical achieved either in magic or physical roll, check if damage contains either
        Set<Map.Entry<String, Double>> entrySet = damageMap.entrySet();
        boolean critConsumed = false;

        for (Map.Entry<String, Double> entry : entrySet) {
            String type = entry.getKey();
            Double amount = entry.getValue();

            boolean phyType = PHYSICAL_ATTACK_TYPES.contains(type);
            if (phyCrit && phyType) {
                entry.setValue(amount * 2);
                critConsumed = true;
            } else if (mgcCrit && !phyType) {
                entry.setValue(amount * 2);
                critConsumed = true;
            }
        }

        if (critConsumed) {
            handleTalentApplyOnApplyType(sourceStats, stats, AttributeApplyType.ON_CRIT_APPLY);
            handleTalentApplyOnApplyType(stats, sourceStats, AttributeApplyType.ON_CRIT_CONSUME);
        }

        return critConsumed;
    }

    public void handleDamageAmp(Map<String, Double> damageMap, Stats stats) {
        Set<Map.Entry<String, Double>> entrySet = damageMap.entrySet();

        Double phyAmp = stats.getDerived(StatsTypes.PHY_AMP);
        Double mgcAmp = stats.getDerived(StatsTypes.MAG_AMP);
        for (Map.Entry<String, Double> entry : entrySet) {
            String type = entry.getKey();
            Double amount = entry.getValue();

            boolean phyType = PHYSICAL_ATTACK_TYPES.contains(type);
            if (phyType) {
                entry.setValue(amount * phyAmp * (1 + rand.nextDouble(0.15)));
            } else {
                entry.setValue(amount * mgcAmp * (1 + rand.nextDouble(0.15)));
            }
        }
    }

    public Stats takeDamage(Stats stats, Map<String, Double> damageMap, Stats sourceStats) {
        // TODO: consider hit chance?
        if (handleDodge(damageMap, stats, sourceStats)) {
            return stats;
        }

        // TODO: handle block event
        // TODO: handle parry event
        // TODO: handle energy resist/absorb event

        handleTalentApplyOnApplyType(sourceStats, stats, AttributeApplyType.ON_HIT_APPLY);
        handleTalentApplyOnApplyType(stats, sourceStats, AttributeApplyType.ON_HIT_CONSUME);

        handleDamageAmp(damageMap, stats);

        boolean isCrit = handleCrit(damageMap, stats, sourceStats);

        // handle damage reduction
        Double totalDamage = damageMap.values().stream().reduce(0.0, Double::sum);

        // handle damage reduction

        if (totalDamage == 0) {
            // no damage taken, ignore
            return stats;
        }

        // negative damage is possible, its a healing effect.

        handleTalentApplyOnApplyType(sourceStats, stats, AttributeApplyType.ON_DMG_APPLY);
        handleTalentApplyOnApplyType(stats, sourceStats, AttributeApplyType.ON_DMG_CONSUME);

        Double currentHp = stats.getDerived(StatsTypes.CURRENT_HP);
        currentHp = Math.min(stats.getDerived(StatsTypes.MAX_HP), currentHp - totalDamage);

        setAndHandleDifference(stats, currentHp, StatsTypes.CURRENT_HP);

        DamageSource damageSource =
                DamageSource.builder()
                        .damageMap(damageMap)
                        .actorId(stats.getActorId())
                        .sourceActorId(sourceStats.getActorId())
                        .additionalData(
                                isCrit
                                        ? DamageAdditionalData.CRIT.getType()
                                        : DamageAdditionalData.HIT.getType())
                        .build();

        log.info("Updating damage, {}, {}, {}", damageSource, stats, sourceStats);

        DamageUpdateMessage update = new DamageUpdateMessage(damageSource, stats, sourceStats);
        updateProducer.updateDamage(update);

        checkActorDeath(update);

        handleThreat(damageMap, stats.getActorId(), sourceStats.getActorId());
        return stats;
    }

    public void checkActorDeath(DamageUpdateMessage update) {
        if (!(update.getTargetStats().getDerived(StatsTypes.CURRENT_HP) <= 0)) {
            return;
        }

        // status service will need to add 'death' state
        // player level stats service needs to add xp
        // require to reset threat level relating to actor (combat service)
        // if its a mob, mob instance server requires to handle it & drop items - done

        updateProducer.notifyActorDeath(update);

        // could push this to the event listener as well
        if (!update.getTargetStats().isPlayer()) {
            deleteStatsFor(update.getTargetStats().getActorId())
                    .doOnError(
                            err ->
                                    log.error(
                                            "Failed to delete stats on death, {}",
                                            err.getMessage()))
                    .delaySubscription(10_000, TimeUnit.MILLISECONDS)
                    .subscribe();
        }

        handleTalentApplyOnApplyType(
                update.getOriginStats(),
                update.getTargetStats(),
                AttributeApplyType.ON_DEATH_APPLY);
        handleTalentApplyOnApplyType(
                update.getTargetStats(),
                update.getOriginStats(),
                AttributeApplyType.ON_DEATH_CONSUME);
    }

    public Stats addHealth(Stats stats, double amount) {
        Double currentHp = stats.getDerived(StatsTypes.CURRENT_HP);
        currentHp += amount;
        setAndHandleDifference(stats, currentHp, StatsTypes.CURRENT_HP);
        return stats;
    }

    public Stats addMana(Stats stats, double amount) {
        Double currentMp = stats.getDerived(StatsTypes.CURRENT_MP);
        currentMp += amount;
        setAndHandleDifference(stats, currentMp, StatsTypes.CURRENT_MP);
        return stats;
    }

    private void setAndHandleDifference(Stats stats, Double val, StatsTypes evalType) {
        stats.getDerivedStats().put(evalType.getType(), val);
        Map<String, Double> updated = Map.of(evalType.getType(), val);
        handleDifference(updated, stats);
    }

    public void sumAndHandleDifference(Stats stats, Double val, String evalType) {
        Double updateVal = stats.getDerivedStats().getOrDefault(evalType, 0.0) + val;
        stats.getDerivedStats().put(evalType, updateVal);
        Map<String, Double> updated = Map.of(evalType, updateVal);
        handleDifference(updated, stats);
    }

    public void applyRegen(String actorName) {
        getStatsFor(actorName)
                .doOnSuccess(
                        stats -> {
                            if (stats == null) {
                                return;
                            }
                            applyRegen(stats);
                        })
                .doOnError(
                        err ->
                                log.error(
                                        "Failed to apply regen for actor: {}, {}",
                                        actorName,
                                        err.getMessage()))
                .subscribe();
    }

    public void applyRegen(Stats stats) {
        applyRegen(stats, StatsTypes.HP_REGEN);
        applyRegen(stats, StatsTypes.MP_REGEN);
    }

    private void applyRegen(Stats stats, StatsTypes type) {
        if (!stats.canAct()) {
            return;
        }

        Double regen = stats.getDerived(type);

        StatsTypes evalType =
                type == StatsTypes.MP_REGEN ? StatsTypes.CURRENT_MP : StatsTypes.CURRENT_HP;

        StatsTypes maxType = type == StatsTypes.MP_REGEN ? StatsTypes.MAX_MP : StatsTypes.MAX_HP;

        Double currentVal = stats.getDerived(evalType);
        Double maxVal = stats.getDerived(maxType);

        if (currentVal >= maxVal) {
            return;
        }

        Double res = currentVal + regen;
        setAndHandleDifference(stats, res, evalType);
    }

    void handleDifference(Map<String, Double> updated, Stats stats) {
        if (!updated.isEmpty()) {
            //          TODO: Make this async, its blocking to help with tests only
            repository
                    .updateStats(stats.getActorId(), stats)
                    .doOnError(err -> log.error("Failed to update stats, {}", err.getMessage()))
                    .blockingSubscribe();
            Stats notifyUpdates =
                    Stats.builder().actorId(stats.getActorId()).derivedStats(updated).build();
            updateProducer.updateStats(notifyUpdates);
        }
    }

    void handleBaseDifference(Map<String, Integer> updated, Stats stats) {
        if (!updated.isEmpty()) {
            //          TODO: Make this async, its blocking to help with tests only
            repository
                    .updateStats(stats.getActorId(), stats)
                    .doOnError(err -> log.error("Failed to update stats, {}", err.getMessage()))
                    .blockingSubscribe();
            Stats notifyUpdates =
                    Stats.builder().actorId(stats.getActorId()).baseStats(updated).build();
            updateProducer.updateStats(notifyUpdates);
        }
    }

    public void evaluateDerivedStats(Stats stats) {
        Map<String, Double> updated = stats.recalculateDerivedStats();
        handleDifference(updated, stats);
    }

    void handleThreat(Map<String, Double> damageMap, String actorTakingDamage, String sourceActor) {
        if (!UUIDHelper.isPlayer(sourceActor) && !UUIDHelper.isPlayer(actorTakingDamage)) {
            return;
        }

        log.info("Adding threat to actor: {}, from actor: {}", actorTakingDamage, sourceActor);

        int totalDamage =
                damageMap.values().stream()
                        .mapToInt(Double::intValue) // Convert each Double to an int
                        .sum();
        // in future, threat can be modified. will be controlled in stats
        threatService
                .addActorThreat(actorTakingDamage, sourceActor, totalDamage)
                .doOnError(
                        err ->
                                log.error(
                                        "Failed to handle threat updates on stats updates, {}",
                                        err.getMessage()))
                .onErrorComplete()
                .subscribe();
    }
}
