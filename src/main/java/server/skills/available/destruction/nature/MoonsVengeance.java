package server.skills.available.destruction.nature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Single;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Stunned;
import server.combat.model.CombatData;
import server.common.dto.Location;
import server.skills.active.aoe.AbstractAoeSkill;
import server.skills.active.aoe.TickingAoeSkill;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Serdeable
@JsonTypeName("Moons vengeance")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class MoonsVengeance extends TickingAoeSkill {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public MoonsVengeance() {
        super(
                "Moons vengeance",
                "Spawn the moon to shoot falling stars onto the area",
                Map.of(StatsTypes.MAGIC_DAMAGE.getType(), 50.0),
                0,
                2000,
                false,
                true,
                1000,
                500,
                Map.of(), 600, 2500,
                4, false
                );
    }


    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {

    }

    @Override
    public boolean canApply(CombatData combatData, SkillTarget skillTarget) {
        return true;
    }

    @Override
    public void applyEffect(CombatData combatData, SkillTarget skillTarget) {
        Stats actorStats = statsService.getStatsFor(combatData.getActorId()).blockingGet();
        Map<String, Double> actorDerived = actorStats.getDerivedStats();

        Double mgcAmp = actorDerived.getOrDefault(StatsTypes.MAG_AMP.getType(), 1.0);

        Double dmgAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());
        dmgAmt = dmgAmt * mgcAmp * (1 + rand.nextDouble(0.15));

        Map<String, Double> damageMap = Map.of(DamageTypes.MAGIC.getType(), dmgAmt);

        actorMotionRepository.fetchActorMotion(skillTarget.getTargetId())
                .doOnSuccess(motion -> getAffectedActors(new Location(motion), combatData.getActorId())
                        .doOnSuccess(actors -> {
                            actors.stream().parallel().forEach(actor -> {
                                Stats targetStats = statsService.getStatsFor(actor).blockingGet();
                                log.info("Applying moons vengeance to: {}, will take: {}", actor, damageMap);
                                targetStats = statsService.takeDamage(targetStats, damageMap, actorStats);
                            });
                        })
                        .subscribe())
                .doOnError(err -> log.error(err.getMessage()))
                .subscribe();
    }
}
