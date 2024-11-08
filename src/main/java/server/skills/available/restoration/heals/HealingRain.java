package server.skills.available.restoration.heals;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
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
                Map.of(),
                1000,
                3000,
                10,
                true
        );
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        // do nothing
    }

    @Override
    public boolean canApply(CombatData combatData, SkillTarget skillTarget) {
        return true;
    }

    @Override
    public void applyEffect(CombatData combatData) {
        Stats actorStats = statsService.getStatsFor(combatData.getActorId()).blockingGet();
        Map<String, Double> actorDerived = actorStats.getDerivedStats();

        Double healAmp = actorDerived.getOrDefault(StatsTypes.MAG_AMP.getType(), 1.0);

        Double healAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());
        healAmt = healAmt * healAmp * (1 + rand.nextDouble(0.15));

        Map<DamageTypes, Double> damageMap = Map.of(DamageTypes.POSITIVE, healAmt);

        actorMotionRepository.fetchActorMotion(combatData.getActorId())
                .doOnSuccess(motion -> getAffectedActors(new Location(motion), combatData.getActorId())
                        .doOnSuccess(actors -> actors.stream().parallel().forEach(actor -> {
                            Stats targetStats = statsService.getStatsFor(actor).blockingGet();
                            log.info("Applying Healing rain to: {}, will take: {}", actor, damageMap);
                            Stats stats = statsService.takeDamage(targetStats, damageMap, combatData.getActorId());
                            checkDeath(stats, combatData.getActorId());
                        }))
                        .subscribe())
                .doOnError(err -> log.error(err.getMessage()))
                .subscribe();
    }
}
