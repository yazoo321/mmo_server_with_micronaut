package server.skills.available.cleric.heals;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.combat.model.CombatData;
import server.common.dto.Location;
import server.skills.active.aoe.TickingAoeSkill;
import server.skills.model.SkillTarget;

import java.util.Map;

@Getter
@JsonTypeName("Healing rain")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class HealingRain extends TickingAoeSkill {

    public HealingRain() {
        super(
                "Healing rain",
                "Channel an aura of tranquility which periodically heals actors nearby",
                Map.of(StatsTypes.MAGIC_DAMAGE.getType(), -20.0),
                1000,
                3000,
                false,
                true,
                500,
                0,
                Map.of(ClassTypes.CLERIC.getType(), 3),
                600,
                3000,
                10,
                true);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        // do nothing
    }

    @Override
    public void applyEffect() {
        CombatData combatData = skillDependencies.getCombatData();
        Double healAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());

        Map<String, Double> damageMap = Map.of(DamageTypes.POSITIVE.getType(), healAmt);

        actorMotionRepository
                .fetchActorMotion(combatData.getActorId())
                .doOnSuccess(
                        motion ->
                                getAffectedPlayers(new Location(motion), combatData.getActorId())
                                        .doOnSuccess(
                                                actors ->
                                                        actors.stream()
                                                                .parallel()
                                                                .forEach(
                                                                        actor -> {
                                                                            log.info(
                                                                                    "Applying"
                                                                                        + " Healing"
                                                                                        + " rain"
                                                                                        + " to: {},"
                                                                                        + " will"
                                                                                        + " take:"
                                                                                        + " {}",
                                                                                    actor,
                                                                                    damageMap);
                                                                            requestTakeDamage(
                                                                                    combatData
                                                                                            .getActorId(),
                                                                                    actor,
                                                                                    damageMap);
                                                                        }))
                                        .subscribe())
                .doOnError(err -> log.error("Healing rain encountered error: {}", err.getMessage()))
                .subscribe();
    }
}
