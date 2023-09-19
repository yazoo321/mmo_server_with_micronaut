package server.combat.service;

import io.micronaut.websocket.WebSocketSession;
import io.netty.util.internal.ConcurrentSet;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import server.attribute.stats.service.StatsService;
import server.attribute.stats.types.DamageTypes;
import server.combat.model.PlayerCombatData;
import server.items.equippable.model.EquippedItems;
import server.items.equippable.service.EquipItemService;
import server.items.types.weapons.Weapon;
import server.session.SessionParamHelper;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static java.time.temporal.ChronoUnit.MILLIS;
import static server.attribute.stats.types.StatsTypes.PHY_AMP;
import static server.attribute.stats.types.StatsTypes.WEAPON_DAMAGE;

@Singleton
public class PlayerCombatService {

    ConcurrentSet<String> sessionsInCombat = new ConcurrentSet<>();

    @Inject
    StatsService statsService;

    public void requestAttack(WebSocketSession session, Set<String> targets) {
        PlayerCombatData combatData = SessionParamHelper.getCombatData(session);
        combatData.setTargets(targets);

        sessionsInCombat.add(SessionParamHelper.getPlayerName(session));
        attackLoop(session);
    }

    public void requestStopAttack(WebSocketSession session) {
        sessionsInCombat.remove(SessionParamHelper.getPlayerName(session));
    }

    private void tryAttackMainHand(WebSocketSession session) {
        PlayerCombatData data = SessionParamHelper.getCombatData(session);
        Instant lastHit = data.getMainHandLastAttack();
        Double baseSpeed = data.getMainHandAttackSpeed();
        Double characterAttkSpeed = data.getCharacterAttackSpeed();

        Double actualDelayInMS = (getAttackTimeDelay(baseSpeed, characterAttkSpeed) * 1000);
        Long actualDelay = actualDelayInMS.longValue();

        if (lastHit.plus(actualDelay, MILLIS).isBefore(Instant.now())) {
            // can attack
            Map<String, Double> derivedStats = SessionParamHelper.getDerivedStats(session);
            Map<String, EquippedItems> items =  SessionParamHelper.getEquippedItems(session);
            EquippedItems weapon = items.get("WEAPON");

            if (weapon == null) {
                return; // potentially support hand-to-hand combat
            }

            Map<String, Double> itemEffects = weapon.getItemInstance().getItem().getItemEffects();
            Double damage = itemEffects.get(WEAPON_DAMAGE.getType());
            double amp = derivedStats.get(PHY_AMP.getType());

            double totalDamage  = damage * amp;
            // future support of damage map
            Map<DamageTypes, Double> damageMap = Map.of(DamageTypes.PHYSICAL, totalDamage);

        }

    }

    private void tryAttackOffHand(WebSocketSession session) {

    }

    private Double getAttackTimeDelay(Double baseAttackSpeed, Double characterAttackSpeed) {
        // 100 attack speed increases speed by 2x
        return baseAttackSpeed / (1 + (characterAttackSpeed / 100));
    }

    private void attackLoop(WebSocketSession session) {
        if (!sessionsInCombat.contains(SessionParamHelper.getPlayerName(session))) {
            return;
        }
        tryAttackMainHand(session);
        tryAttackOffHand(session);

        Single.fromCallable(() -> {
            attackLoop(session);
            return null;
        }).delaySubscription(100, TimeUnit.MILLISECONDS).subscribe();

    }

//    public static void armorCalculation();

}
