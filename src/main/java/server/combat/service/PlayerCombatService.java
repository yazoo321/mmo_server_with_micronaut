package server.combat.service;

import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.service.StatusService;
import server.combat.model.PlayerCombatData;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.service.EquipItemService;
import server.items.types.weapons.Weapon;
import server.session.SessionParamHelper;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MILLIS;
import static server.attribute.stats.types.StatsTypes.PHY_AMP;
import static server.attribute.stats.types.StatsTypes.WEAPON_DAMAGE;

@Singleton
public class PlayerCombatService {

    ConcurrentSet<String> sessionsInCombat = new ConcurrentSet<>();

    @Inject
    StatsService statsService;

    @Inject
    StatusService statusService;

    public void requestAttack(WebSocketSession session, Set<String> targets) {
        PlayerCombatData combatData = SessionParamHelper.getCombatData(session);
        combatData.setTargets(targets);

        sessionsInCombat.add(SessionParamHelper.getPlayerName(session));
        attackLoop(session);
    }

    public void requestStopAttack(WebSocketSession session) {
        sessionsInCombat.remove(SessionParamHelper.getPlayerName(session));
    }

    private void tryAttackMainHand(WebSocketSession session, Stats target) {
        PlayerCombatData data = SessionParamHelper.getCombatData(session);

        // Extract relevant combat data
        Instant lastHit = data.getMainHandLastAttack();
        Double baseSpeed = data.getMainHandAttackSpeed();
        Double characterAttackSpeed = data.getCharacterAttackSpeed();

        // Calculate the actual delay in milliseconds
        long actualDelayInMS = (long) (getAttackTimeDelay(baseSpeed, characterAttackSpeed) * 1000);

        // Calculate the next allowed attack time
        Instant nextAttackTime = lastHit.plusMillis(actualDelayInMS);

        // Check if the next attack time is before the current time
        if (nextAttackTime.isBefore(Instant.now())) {
            // The player can attack

            // Get derived stats and equipped items
            Map<String, Double> derivedStats = SessionParamHelper.getDerivedStats(session);
            Map<String, EquippedItems> items = SessionParamHelper.getEquippedItems(session);

            // Get the equipped weapon
            EquippedItems weapon = items.get("WEAPON");

            if (weapon != null) {
                // Create a damage map (currently only physical damage)
                Map<DamageTypes, Double> damageMap = calculateDamageMapBasedOnWeapon(weapon, derivedStats);
                statsService.takeDamage(target, damageMap);
            }
        }
    }
    private void tryAttackOffHand(WebSocketSession session, Stats target) {
        PlayerCombatData data = SessionParamHelper.getCombatData(session);

        // Extract relevant combat data
        Instant lastHit = data.getOffhandLastAttack();
        Double baseSpeed = data.getOffhandAttackSpeed();
        Double characterAttackSpeed = data.getCharacterAttackSpeed();

        // Calculate the actual delay in milliseconds
        long actualDelayInMS = (long) (getAttackTimeDelay(baseSpeed, characterAttackSpeed) * 1000);

        // Calculate the next allowed attack time
        Instant nextAttackTime = lastHit.plusMillis(actualDelayInMS);

        // Check if the next attack time is before the current time
        if (nextAttackTime.isBefore(Instant.now())) {
            // The player can attack

            // Get derived stats and equipped items
            Map<String, Double> derivedStats = SessionParamHelper.getDerivedStats(session);
            Map<String, EquippedItems> items = SessionParamHelper.getEquippedItems(session);

            // Get the equipped weapon
            EquippedItems weapon = items.get("SHIELD");

            if (weapon != null) {
                // Create a damage map (currently only physical damage)
                Map<DamageTypes, Double> damageMap = calculateDamageMapBasedOnWeapon(weapon, derivedStats);
                statsService.takeDamage(target, damageMap);
            }
        }
    }

    private Map<DamageTypes, Double> calculateDamageMapBasedOnWeapon(EquippedItems weapon, Map<String, Double> derivedStats) {
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

        // check target
        PlayerCombatData combatData = SessionParamHelper.getCombatData(session);
        Set<String> targets = combatData.getTargets();

        List<Stats> targetStats = getTargetStats(targets);
        // we add support for multiple, but we expect only one target

        targetStats.forEach(stat -> {
            tryAttackMainHand(session, stat);
            tryAttackOffHand(session, stat);
        });


        Single.fromCallable(() -> {
            attackLoop(session);
            return null;
        }).delaySubscription(100, TimeUnit.MILLISECONDS).subscribe();

    }

    private List<Stats> getTargetStats(Set<String> actors) {
        List<Stats> targetStats = new ArrayList<>();

        return actors.stream()
                .map(actor -> statsService.getStatsFor(actor).blockingGet())
                .filter(s -> s.getDerivedStats().get(StatsTypes.CURRENT_HP.getType()) > 0)
                .collect(Collectors.toList());
    }

}
