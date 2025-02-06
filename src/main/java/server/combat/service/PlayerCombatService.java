package server.combat.service;

import static server.attribute.stats.types.StatsTypes.PHY_AMP;
import static server.attribute.stats.types.StatsTypes.WEAPON_DAMAGE;

import io.micronaut.websocket.WebSocketSession;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.ActorStatus;
import server.combat.model.CombatData;
import server.combat.model.CombatRequest;
import server.combat.model.CombatState;
import server.common.dto.Motion;
import server.items.equippable.model.EquippedItems;
import server.session.SessionParamHelper;
import server.socket.service.ClientUpdatesService;

@Slf4j
@Singleton
public class PlayerCombatService extends CombatService {

    @Inject ClientUpdatesService clientUpdatesService;

    Random rand = new Random();

    public void requestAttack(WebSocketSession session, CombatRequest combatRequest) {
        if (combatRequest == null) {
            return;
        }

        String actorId = SessionParamHelper.getActorId(session);
        if (combatRequest.getTargets() == null || combatRequest.getTargets().size() != 1) {
            log.warn("Only support 1 combat target");
            combatRequest.setTargets(
                    new HashSet<>(Set.of(combatRequest.getTargets().iterator().next())));
        }
        canEngageCombat(actorId, combatRequest.getTargets().iterator().next())
                .doOnSuccess(
                        canEngage -> {
                            if (!canEngage) {
                                return;
                            }

                            CombatData combatData = combatDataCache.fetchCombatData(actorId);
                            combatData.setTargets(combatRequest.getTargets());

                            if (sessionsInCombat.contains(SessionParamHelper.getActorId(session))) {
                                combatDataCache.cacheCombatData(
                                        SessionParamHelper.getActorId(session), combatData);
                                // this can mean a change of target, we want to update the combat
                                // data but not to start another attack loop
                                return;
                            }
                            sessionsInCombat.add(SessionParamHelper.getActorId(session));
                            combatDataCache.cacheCombatData(
                                    SessionParamHelper.getActorId(session), combatData);
                            attackLoop(session, combatData.getActorId());
                        })
                .subscribe();
    }

    public void requestStopAttack(String actorId) {
        sessionsInCombat.remove(actorId);
    }

    // TODO: Refactor this with MobCombatService tryAttack; there is a large overlap
    public void tryAttack(WebSocketSession session, final Stats target, boolean isMainHand) {
        // Extract relevant combat data
        String actorId = SessionParamHelper.getActorId(session);
        CombatData combatData = combatDataCache.fetchCombatData(actorId);

        Single<Map<String, EquippedItems>> itemsSingle =
                equipItemService.getEquippedItemsMap(actorId);
        Single<Stats> actorStatsSingle = statsService.getStatsFor(actorId);
        Single<ActorStatus> actorStatusSingle = statusService.getActorStatus(actorId);
        Single<ActorStatus> targetStatusSingle = statusService.getActorStatus(target.getActorId());
        Single<Motion> actorMotionSingle = actorMotionRepository.fetchActorMotion(actorId);

        Single.zip(
                itemsSingle,
                actorStatsSingle,
                actorStatusSingle,
                targetStatusSingle,
                actorMotionSingle,
                (actorItems, actorStats, actorStatus, targetStatus, actorMotion) -> {
                    if (actorStatus.isDead() || targetStatus.isDead()) {
                        sessionsInCombat.remove(actorId);
                        return 1;
                    }
                    if (!actorStatus.canAttack()) {
                        // may be stunned, try again later
                        return 1;
                    }

                    Map<String, Double> derivedStats = actorStats.getDerivedStats();

                    // Get the equipped weapon
                    EquippedItems weapon =
                            isMainHand ? actorItems.get("WEAPON") : actorItems.get("SHIELD");
                    if (weapon == null) {
                        return 1;
                    }

                    int distanceThreshold =
                            weapon.getAttackDistance() == null
                                    ? 200
                                    : (int) (double) weapon.getAttackDistance();

                    boolean valid = actorCombatStateValid(combatData.getCombatState());

                    valid =
                            valid
                                    && validatePositionLocation(
                                            combatData,
                                            actorMotion,
                                            target.getActorId(),
                                            distanceThreshold,
                                            session);

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
                        requestAttackSwing(session, combatData, isMainHand);
                    }

                    Double baseSpeed =
                            isMainHand
                                    ? derivedStats.get(StatsTypes.MAIN_HAND_ATTACK_SPEED.getType())
                                    : derivedStats.get(StatsTypes.OFF_HAND_ATTACK_SPEED.getType());

                    if (baseSpeed == null || baseSpeed == 0.0) {
                        log.warn(
                                "Base attack speed is null while on attack of mainhand == {}",
                                isMainHand);
                        return 1;
                    }

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
                            requestAttackSwing(session, combatData, isMainHand);
                        }

                        combatData.getAttackSent().put(isMainHand ? "MAIN" : "OFF", false);

                        // TODO: implement dodge/block and insert here

                        // Create a damage map (currently only physical damage)
                        Map<String, Double> damageMap = calculateDamageMap(weapon, derivedStats);

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
                        requestAttackSwing(session, combatData, isMainHand);

                        combatDataCache.cacheCombatData(actorId, combatData);
                    }
                    return 1;
                });
    }

    void attackLoop(WebSocketSession session, String actorId) {
        CombatData combatData = combatDataCache.fetchCombatData(actorId);
        Set<String> targets = combatData.getTargets();

        List<Stats> targetStats = getTargetStats(targets);

        if (targetStats.isEmpty()) {
            log.warn("Target stats empty");
            sessionsInCombat.remove(SessionParamHelper.getActorId(session));
            return;
        }

        targetStats.forEach(
                target -> {
                    tryAttack(session, target, true);
                    //            tryAttack(session, stat, "OFF_HAND");
                });

        Single.fromCallable(
                        () -> {
                            attackLoop(session, SessionParamHelper.getActorId(session));
                            return true;
                        })
                .delaySubscription(100, TimeUnit.MILLISECONDS)
                .doOnError(
                        er -> {
                            log.error("Error encountered, {}", er.getMessage());
                            sessionsInCombat.remove(SessionParamHelper.getActorId(session));
                        })
                .subscribe();
    }

    private void requestAttackSwing(
            WebSocketSession session, CombatData combatData, boolean isMainHand) {
        String actorId = SessionParamHelper.getActorId(session);
        Map<String, EquippedItems> items =
                equipItemService.getEquippedItemsMap(actorId).blockingGet();

        // Get the equipped weapon
        EquippedItems weapon = isMainHand ? items.get("WEAPON") : items.get("SHIELD");
        String itemInstanceId = weapon.getItemInstance().getItemInstanceId();

        combatData.getAttackSent().put(isMainHand ? "MAIN" : "OFF", true);

        requestSessionsToSwingWeapon(itemInstanceId, SessionParamHelper.getActorId(session));
    }

    List<String> validAttackStates =
            List.of(CombatState.IDLE.getType(), CombatState.ATTACKING.getType());

    private boolean actorCombatStateValid(String state) {
        return validAttackStates.contains(state);
    }

    private Map<String, Double> calculateDamageMap(
            EquippedItems weapon, Map<String, Double> derivedStats) {
        // Calculate damage based on weapon and stats
        Map<String, Double> itemEffects = weapon.getItemInstance().getItem().getItemEffects();
        Double damage = itemEffects.get(WEAPON_DAMAGE.getType());
        double amp = derivedStats.get(PHY_AMP.getType());

        double totalDamage = Math.floor(damage * amp * (1 + rand.nextDouble(0.15)));

        // Create a damage map (currently only physical damage)
        return Map.of(DamageTypes.PHYSICAL.getType(), totalDamage);
    }

    private Double getAttackTimeDelay(Double baseAttackSpeed, Double characterAttackSpeed) {
        // 100 attack speed increases speed by 2x
        return baseAttackSpeed / (1 + (characterAttackSpeed / 100));
    }
}
