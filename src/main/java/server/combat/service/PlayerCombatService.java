package server.combat.service;

import static server.attribute.stats.types.StatsTypes.PHY_AMP;
import static server.attribute.stats.types.StatsTypes.WEAPON_DAMAGE;

import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.service.StatusService;
import server.combat.model.CombatRequest;
import server.combat.model.PlayerCombatData;
import server.common.dto.Motion;
import server.items.equippable.model.EquippedItems;
import server.monster.server_integration.model.Monster;
import server.monster.server_integration.service.MobInstanceService;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;
import server.socket.service.ClientUpdatesService;

@Slf4j
@Singleton
public class PlayerCombatService {

    private final ConcurrentSet<String> sessionsInCombat = new ConcurrentSet<>();

    @Inject private StatsService statsService;

    @Inject private StatusService statusService;

    @Inject private MobInstanceService mobInstanceService;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    @Inject ClientUpdatesService clientUpdatesService;

    @Inject SessionParamHelper sessionParamHelper;

    Random rand = new Random();

    public void requestAttack(WebSocketSession session, CombatRequest combatRequest) {
        if (combatRequest == null) {
            return;
        }
        PlayerCombatData combatData = SessionParamHelper.getCombatData(session);
        combatData.setTargets(combatRequest.getTargets());

        sessionsInCombat.add(SessionParamHelper.getPlayerName(session));
        attackLoop(session);
    }

    public void requestStopAttack(WebSocketSession session) {
        sessionsInCombat.remove(SessionParamHelper.getPlayerName(session));
    }

    private void tryAttack(WebSocketSession session, Stats target, boolean isMainHand) {
        // Extract relevant combat data
        PlayerCombatData data = SessionParamHelper.getCombatData(session);
        Map<String, EquippedItems> items = SessionParamHelper.getEquippedItems(session);
        Map<String, Double> derivedStats = SessionParamHelper.getDerivedStats(session);

        // Get the equipped weapon
        EquippedItems weapon = isMainHand ? items.get("WEAPON") : items.get("SHIELD");
        if (weapon == null) {
            return;
        }

        int distanceThreshold =
                weapon.getAttackDistance() == null
                        ? 200
                        : (int) (double) weapon.getAttackDistance();
        boolean valid = validatePositionLocation(session, target.getActorId(), distanceThreshold);

        if (!valid) {
            return;
        }

        Instant lastHit = isMainHand ? data.getMainHandLastAttack() : data.getOffhandLastAttack();
        // TODO: this is for demo, needs changing
        if (lastHit == null || lastHit.isBefore(Instant.now().minusSeconds(4))) {
            lastHit = Instant.now().minusSeconds(4);
            requestAttackSwing(session, isMainHand);
        }
        Double baseSpeed =
                isMainHand ? data.getMainHandAttackSpeed() : data.getOffhandAttackSpeed();
        Double characterAttackSpeed = data.getCharacterAttackSpeed();

        // Calculate the actual delay in milliseconds
        long actualDelayInMS = (long) (getAttackTimeDelay(baseSpeed, characterAttackSpeed) * 1000);

        // Calculate the next allowed attack time
        Instant nextAttackTime = lastHit.plusMillis(actualDelayInMS);

        if (nextAttackTime.isBefore(Instant.now())) {
            // The player can attack
            // Get derived stats and equipped items

            // Create a damage map (currently only physical damage)
            Map<DamageTypes, Double> damageMap = calculateDamageMap(weapon, derivedStats);
            Stats stats = statsService.takeDamage(target, damageMap);
            if (isMainHand) {
                data.setMainHandLastAttack(Instant.now());
            } else {
                data.setOffhandLastAttack(Instant.now());
            }

            if (stats.getDerived(StatsTypes.CURRENT_HP) <= 0.0) {
                statsService
                        .deleteStatsFor(stats.getActorId())
                        .doOnError(
                                err ->
                                        log.error(
                                                "Failed to delete stats on death, {}",
                                                err.getMessage()))
                        .subscribe();
                mobInstanceService.handleMobDeath(stats.getActorId());
                clientUpdatesService.notifyServerOfRemovedMobs(Set.of(stats.getActorId()));
                SessionParamHelper.getCombatData(session).getTargets().remove(target.getActorId());
            }

            return;
        }

        // Check if the next attack time is before the current time
        if (nextAttackTime.isBefore(Instant.now().plusMillis(100))) {
            // send a swing action as we're about to hit - we don't know if we will hit or miss yet
            requestAttackSwing(session, isMainHand);
        }
    }

    private void requestAttackSwing(WebSocketSession session, boolean isMainHand) {
        Map<String, EquippedItems> items = SessionParamHelper.getEquippedItems(session);

        // Get the equipped weapon
        EquippedItems weapon = isMainHand ? items.get("WEAPON") : items.get("SHIELD");
        String itemInstanceId = weapon.getItemInstance().getItemInstanceId();

        requestSessionToSwingWeapon(session, itemInstanceId);
    }

    private void requestSessionToSwingWeapon(WebSocketSession session, String itemInstanceId) {
        CombatRequest request = new CombatRequest();
        request.setItemInstanceId(itemInstanceId);
        session.send(
                        SocketResponse.builder()
                                .messageType(SocketResponseType.INITIATE_ATTACK.getType())
                                .combatRequest(request)
                                .build())
                .subscribe(socketResponseSubscriber);
    }

    private Map<DamageTypes, Double> calculateDamageMap(
            EquippedItems weapon, Map<String, Double> derivedStats) {
        // Calculate damage based on weapon and stats
        Map<String, Double> itemEffects = weapon.getItemInstance().getItem().getItemEffects();
        Double damage = itemEffects.get(WEAPON_DAMAGE.getType());
        double amp = derivedStats.get(PHY_AMP.getType());

        double totalDamage = damage * amp * (1 + rand.nextDouble(0.15));

        // Create a damage map (currently only physical damage)
        return Map.of(DamageTypes.PHYSICAL, totalDamage);
    }

    private Double getAttackTimeDelay(Double baseAttackSpeed, Double characterAttackSpeed) {
        // 100 attack speed increases speed by 2x
        return baseAttackSpeed / (1 + (characterAttackSpeed / 100));
    }

    private void attackLoop(WebSocketSession session) {
        if (!sessionsInCombat.contains(SessionParamHelper.getPlayerName(session))) {
            log.warn("left combat");
            return;
        }

        PlayerCombatData combatData = SessionParamHelper.getCombatData(session);
        Set<String> targets = combatData.getTargets();

        List<Stats> targetStats = getTargetStats(targets);

        if (targetStats.isEmpty()) {
            log.warn("Target stats empty");
            sessionsInCombat.remove(SessionParamHelper.getPlayerName(session));
            return;
        }

        targetStats.forEach(
                stat -> {
                    tryAttack(session, stat, true);
                    //            tryAttack(session, stat, "OFF_HAND");
                });

        Single.fromCallable(
                        () -> {
                            attackLoop(session);
                            return true;
                        })
                .delaySubscription(100, TimeUnit.MILLISECONDS)
                .doOnError(er -> log.error("Error encountered, {}", er.getMessage()))
                .subscribe();
    }

    private List<Stats> getTargetStats(Set<String> actors) {
        // TODO: Make async
        if (actors.isEmpty()) {
            return new ArrayList<>();
        }
        return actors.stream()
                .map(actor -> statsService.getStatsFor(actor).blockingGet())
                .filter(s -> s.getDerivedStats().get(StatsTypes.CURRENT_HP.getType()) > 0)
                .collect(Collectors.toList());
    }

    private boolean validatePositionLocation(
            WebSocketSession session, String mob, int distanceThreshold) {
        // TODO: Refactor mob/player motion calls
        // TODO: Make async

        List<Monster> res = mobInstanceService.getMobsByIds(Set.of(mob)).blockingGet();
        PlayerCombatData combatData = SessionParamHelper.getCombatData(session);

        if (res.isEmpty()) {
            combatData.getTargets().remove(mob);

            return false;
        }

        Monster monster = res.get(0);

        Motion targetMotion = monster.getMotion();
        Motion attackerMotion =
                sessionParamHelper.getSharedActorMotion(SessionParamHelper.getPlayerName(session));

        boolean inRange = attackerMotion.withinRange(targetMotion, distanceThreshold);
        boolean facingTarget = attackerMotion.facingMotion(targetMotion);

        if (!inRange || !facingTarget) {
            if (combatData.getLastHelperNotification() == null
                    || Instant.now().getEpochSecond()
                                    - combatData.getLastHelperNotification().getEpochSecond()
                            > 3) {
                combatData.setLastHelperNotification(Instant.now());

                if (!inRange) {
                    clientUpdatesService.notifySessionCombatTooFar(session);
                    return false;
                }
                clientUpdatesService.notifySessionCombatNotFacing(session);
            }
            return false;
        }

        return true;
    }
}
