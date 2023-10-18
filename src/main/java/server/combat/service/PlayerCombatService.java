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
import server.items.equippable.model.EquippedItems;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class PlayerCombatService {

    private final ConcurrentSet<String> sessionsInCombat = new ConcurrentSet<>();

    @Inject private StatsService statsService;

    @Inject private StatusService statusService;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    public void requestAttack(WebSocketSession session, CombatRequest combatRequest) {
        if (combatRequest == null) {
            log.error("Combat data is empty");
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
        PlayerCombatData data = SessionParamHelper.getCombatData(session);

        // Extract relevant combat data
        Instant lastHit = isMainHand ? data.getMainHandLastAttack() : data.getOffhandLastAttack();
        if (lastHit == null) {
            // TODO: this can be error prone
            lastHit = Instant.now().minusSeconds(10);
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
            Map<String, Double> derivedStats = SessionParamHelper.getDerivedStats(session);
            Map<String, EquippedItems> items = SessionParamHelper.getEquippedItems(session);

            // Get the equipped weapon
            EquippedItems weapon = isMainHand ? items.get("WEAPON") : items.get("SHIELD");

            if (weapon != null) {
                // Create a damage map (currently only physical damage)
                Map<DamageTypes, Double> damageMap = calculateDamageMap(weapon, derivedStats);
                statsService.takeDamage(target, damageMap);
                if (isMainHand) {
                    data.setMainHandLastAttack(Instant.now());
                } else {
                    data.setOffhandLastAttack(Instant.now());
                }

                return;
            }
        }

        // Check if the next attack time is before the current time
        if (nextAttackTime.isBefore(Instant.now().plusMillis(100))) {
            // send a swing action as we're about to hit - we don't know if we will hit or miss yet

            Map<String, EquippedItems> items = SessionParamHelper.getEquippedItems(session);

            // Get the equipped weapon
            EquippedItems weapon = isMainHand ? items.get("WEAPON") : items.get("SHIELD");
            String itemInstanceId = weapon.getItemInstance().getItemInstanceId();

            requestSessionToSwingWeapon(session, itemInstanceId);
        }
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

        double totalDamage = damage * amp;

        // Create a damage map (currently only physical damage)
        return Map.of(DamageTypes.PHYSICAL, totalDamage);
    }

    private Double getAttackTimeDelay(Double baseAttackSpeed, Double characterAttackSpeed) {
        // 100 attack speed increases speed by 2x
        return baseAttackSpeed / (1 + (characterAttackSpeed / 100));
    }

    private void attackLoop(WebSocketSession session) {
        if (!sessionsInCombat.contains(SessionParamHelper.getPlayerName(session))) {
            return;
        }

        PlayerCombatData combatData = SessionParamHelper.getCombatData(session);
        Set<String> targets = combatData.getTargets();

        List<Stats> targetStats = getTargetStats(targets);

        if (targetStats.isEmpty()) {
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
                            return null;
                        })
                .delaySubscription(100, TimeUnit.MILLISECONDS)
                .subscribe();
    }

    private List<Stats> getTargetStats(Set<String> actors) {
        return actors.stream()
                .map(actor -> statsService.getStatsFor(actor).blockingGet())
                .filter(s -> s.getDerivedStats().get(StatsTypes.CURRENT_HP.getType()) > 0)
                .collect(Collectors.toList());
    }
}
