package server.combat.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.common.dto.Motion;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static server.attribute.stats.types.StatsTypes.PHY_AMP;
import static server.attribute.stats.types.StatsTypes.WEAPON_DAMAGE;

@Slf4j
@Singleton
public class MobCombatService extends CombatService  {

    Random rand = new Random();

    public void requestAttack(CombatRequest combatRequest) {
        if (combatRequest == null || combatRequest.getTargets().isEmpty()) {
            return;
        }
        String targetActorId = combatRequest.getTargets().stream().findFirst().get();
        String actorId = combatRequest.getActorId();
        CombatData combatData = sessionParamHelper.getSharedActorCombatData(actorId);
        combatData.setTargets(combatRequest.getTargets());

        sessionsInCombat.add(actorId);
        sessionParamHelper.setSharedActorCombatData(actorId, combatData);
        attackLoop(actorId);
    }

    public void requestStopAttack(String actorId) {
        sessionsInCombat.remove(actorId);
    }

    public void tryAttack(String actorId, Stats target, boolean isMainHand) {
        // Extract relevant combat data
        CombatData combatData = sessionParamHelper.getSharedActorCombatData(actorId);
        Map<String, Double> derivedStats = combatData.getDerivedStats();
        int distanceThreshold = 200;
        Motion attackerMotion = sessionParamHelper.getSharedActorMotion(actorId);

        boolean valid = validatePositionLocation(combatData, attackerMotion, target.getActorId(),
                distanceThreshold, null);

        if (!valid) {
            return;
        }

        Instant lastHit =
                isMainHand ? combatData.getMainHandLastAttack() : combatData.getOffhandLastAttack();
        // TODO: this is for demo, needs changing
        if (lastHit == null || lastHit.isBefore(Instant.now().minusSeconds(4))) {
            lastHit = Instant.now().minusSeconds(4);
            requestAttackSwing(actorId, combatData, isMainHand);
        }

        Double baseSpeed =
                isMainHand
                        ? derivedStats.get(StatsTypes.MAIN_HAND_ATTACK_SPEED.getType())
                        : derivedStats.get(StatsTypes.OFF_HAND_ATTACK_SPEED.getType());
        Double characterAttackSpeed = derivedStats.get(StatsTypes.ATTACK_SPEED.getType());

        // Calculate the actual delay in milliseconds
        long actualDelayInMS = (long) (getAttackTimeDelay(baseSpeed, characterAttackSpeed) * 1000);

        // Calculate the next allowed attack time
        Instant nextAttackTime = lastHit.plusMillis(actualDelayInMS);

        if (nextAttackTime.isBefore(Instant.now())) {
            // The player can attack
            // Get derived stats and equipped items

            if (!combatData.getAttackSent().getOrDefault(isMainHand ? "MAIN" : "OFF", false)) {
                requestAttackSwing(actorId, combatData, isMainHand);
            }

            combatData.getAttackSent().put(isMainHand ? "MAIN" : "OFF", false);

            // Create a damage map (currently only physical damage)
            Map<DamageTypes, Double> damageMap = calculateDamageMap(derivedStats);
            Stats stats = statsService.takeDamage(target, damageMap);
            if (isMainHand) {
                combatData.setMainHandLastAttack(Instant.now());
            } else {
                combatData.setOffhandLastAttack(Instant.now());
            }

            if (stats.getDerived(StatsTypes.CURRENT_HP) <= 0.0) {
                handleActorDeath(stats);
                combatData.getTargets().remove(target.getActorId());
            }

            sessionParamHelper.setSharedActorCombatData(actorId, combatData);
            return;
        }

        // Check if the next attack time is before the current time
        if (nextAttackTime.isBefore(Instant.now().plusMillis(100))) {
            // send a swing action as we're about to hit - we don't know if we will hit or miss yet
            requestAttackSwing(actorId, combatData, isMainHand);
            sessionParamHelper.setSharedActorCombatData(actorId, combatData);
        }
    }

    void attackLoop(String actorId) {
        CombatData combatData = sessionParamHelper.getSharedActorCombatData(actorId);
        if (combatData == null) {
            // mob likely died, later we can specify death state to be certain instead.
            sessionsInCombat.remove(actorId);
        }

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
                .doOnError(er -> log.error("Error encountered, {}", er.getMessage()))
                .subscribe();
    }

    private void requestAttackSwing(String actorId, CombatData combatData, boolean isMainHand) {
        // Get the equipped weapon
        combatData.getAttackSent().put(isMainHand ? "MAIN" : "OFF", true);
        requestSessionsToSwingWeapon(null, actorId);
    }

    private Map<DamageTypes, Double> calculateDamageMap(Map<String, Double> derivedStats) {
        double damage = derivedStats.get(WEAPON_DAMAGE.getType());
        double amp = derivedStats.get(PHY_AMP.getType());
        double totalDamage = damage * amp * (1 + rand.nextDouble(0.15));

        // Create a damage map (currently only physical damage)
        return Map.of(DamageTypes.PHYSICAL, totalDamage);
    }

    private Double getAttackTimeDelay(Double baseAttackSpeed, Double characterAttackSpeed) {
        // 100 attack speed increases speed by 2x
        return baseAttackSpeed / (1 + (characterAttackSpeed / 100));
    }

}
