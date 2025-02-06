package server.combat.service;

import static server.attribute.stats.types.StatsTypes.PHY_AMP;
import static server.attribute.stats.types.StatsTypes.WEAPON_DAMAGE;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.ActorStatus;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.common.dto.Motion;

@Slf4j
@Singleton
public class MobCombatService extends CombatService {

    Random rand = new Random();

    public void requestAttack(CombatRequest combatRequest) {
        if (combatRequest == null || combatRequest.getTargets().isEmpty()) {
            return;
        }
        String targetActorId = combatRequest.getTargets().stream().findFirst().get();
        String actorId = combatRequest.getActorId();

        CombatData combatData = combatDataCache.fetchCombatData(actorId);
        combatData.setTargets(combatRequest.getTargets());

        if (sessionsInCombat.contains(actorId)) {
            combatDataCache.cacheCombatData(actorId, combatData);
            // this can mean a change of target, we want to update the combat data but not to start
            // another attack loop
            return;
        }
        sessionsInCombat.add(actorId);
        combatDataCache.cacheCombatData(actorId, combatData);
        attackLoop(actorId);
    }

    public void requestStopAttack(String actorId) {
        sessionsInCombat.remove(actorId);
    }

    public void tryAttack(String actorId, final Stats target, boolean isMainHand) {
        // Extract relevant combat data
        CombatData combatData = combatDataCache.fetchCombatData(actorId);
        Single<Stats> actorStatsSingle = statsService.getStatsFor(actorId);
        Single<Motion> actorMotionSingle = actorMotionRepository.fetchActorMotion(actorId);
        Single<ActorStatus> actorStatusSingle = statusService.getActorStatus(actorId);
        Single<ActorStatus> targetStatusSingle = statusService.getActorStatus(target.getActorId());

        Single.zip(
                actorStatsSingle,
                actorMotionSingle,
                actorStatusSingle,
                targetStatusSingle,
                (actorStats, actorMotion, actorStatus, targetStatus) -> {
                    if (actorStatus.isDead() || targetStatus.isDead()) {
                        sessionsInCombat.remove(actorId);
                        return 1;
                    }
                    if (!actorStatus.canAttack()) {
                        // stunned or disarmed, try later
                        return 1;
                    }

                    Map<String, Double> derivedStats = actorStats.getDerivedStats();
                    int distanceThreshold = 250;

                    boolean valid =
                            validatePositionLocation(
                                    combatData,
                                    actorMotion,
                                    target.getActorId(),
                                    distanceThreshold,
                                    null);

                    if (!valid) {
                        return 1;
                    }

                    Instant lastHit =
                            isMainHand
                                    ? combatData.getMainHandLastAttack()
                                    : combatData.getOffhandLastAttack();
                    // TODO: this is for demo, needs changing
                    if (lastHit == null || lastHit.isBefore(Instant.now().minusSeconds(4))) {
                        lastHit = Instant.now().minusSeconds(4);
                        requestAttackSwing(actorId, combatData, isMainHand);
                    }

                    Double baseSpeed =
                            isMainHand
                                    ? derivedStats.get(StatsTypes.MAIN_HAND_ATTACK_SPEED.getType())
                                    : derivedStats.get(StatsTypes.OFF_HAND_ATTACK_SPEED.getType());
                    Double characterAttackSpeed =
                            derivedStats.get(StatsTypes.ATTACK_SPEED.getType());

                    // Calculate the actual delay in milliseconds
                    long actualDelayInMS =
                            (long) (getAttackTimeDelay(baseSpeed, characterAttackSpeed) * 1000);

                    // Calculate the next allowed attack time
                    Instant nextAttackTime = lastHit.plusMillis(actualDelayInMS);

                    if (nextAttackTime.isBefore(Instant.now())) {
                        // The player can attack
                        // Get derived stats and equipped items

                        if (!combatData
                                .getAttackSent()
                                .getOrDefault(isMainHand ? "MAIN" : "OFF", false)) {
                            requestAttackSwing(actorId, combatData, isMainHand);
                        }

                        combatData.getAttackSent().put(isMainHand ? "MAIN" : "OFF", false);

                        // Create a damage map (currently only physical damage)
                        Map<String, Double> damageMap = calculateDamageMap(derivedStats);
                        statsService.takeDamage(target, damageMap, actorStats);
                        if (isMainHand) {
                            combatData.setMainHandLastAttack(Instant.now());
                        } else {
                            combatData.setOffhandLastAttack(Instant.now());
                        }

                        combatDataCache.cacheCombatData(actorId, combatData);
                        return 1;
                    }

                    // Check if the next attack time is before the current time
                    if (nextAttackTime.isBefore(Instant.now().plusMillis(100))) {
                        // send a swing action as we're about to hit - we don't know if we will hit
                        // or miss yet
                        requestAttackSwing(actorId, combatData, isMainHand);
                        combatDataCache.cacheCombatData(actorId, combatData);
                    }

                    return 1;
                });
    }

    void attackLoop(String actorId) {
        try {
            CombatData combatData = combatDataCache.fetchCombatData(actorId);
            if (combatData == null) {
                // mob likely died, later we can specify death state to be certain instead.
                sessionsInCombat.remove(actorId);
            }

            // TODO: Consider simplifying by only having 1 target
            Set<String> targets = combatData.getTargets();

            List<Stats> targetStats = getTargetStats(targets);

            if (targetStats.isEmpty()) {
                log.warn("Target stats empty");
                sessionsInCombat.remove(actorId);
                return;
            }

            targetStats.forEach(
                    stat -> {
                        tryAttack(actorId, stat, true);
                        //            tryAttack(session, stat, "OFF_HAND");
                    });

            Single.fromCallable(
                            () -> {
                                attackLoop(actorId);
                                return true;
                            })
                    .delaySubscription(100, TimeUnit.MILLISECONDS)
                    .doOnError(
                            er -> {
                                log.error("Error encountered, {}", er.getMessage());
                                sessionsInCombat.remove(actorId);
                            })
                    .subscribe();
        } catch (Exception e) {
            log.error("Exception in attack loop: {}", e.getMessage());
            sessionsInCombat.remove(actorId);
        }
    }

    private void requestAttackSwing(String actorId, CombatData combatData, boolean isMainHand) {
        // Get the equipped weapon
        combatData.getAttackSent().put(isMainHand ? "MAIN" : "OFF", true);
        requestSessionsToSwingWeapon(null, actorId);
    }

    private Map<String, Double> calculateDamageMap(Map<String, Double> derivedStats) {
        double damage = derivedStats.get(WEAPON_DAMAGE.getType());
        double amp = derivedStats.get(PHY_AMP.getType());
        double totalDamage = damage * amp * (1 + rand.nextDouble(0.15));

        // Create a damage map (currently only physical damage)
        return Map.of(DamageTypes.PHYSICAL.getType(), totalDamage);
    }

    private Double getAttackTimeDelay(Double baseAttackSpeed, Double characterAttackSpeed) {
        // 100 attack speed increases speed by 2x
        return baseAttackSpeed / (1 + (characterAttackSpeed / 100));
    }
}
