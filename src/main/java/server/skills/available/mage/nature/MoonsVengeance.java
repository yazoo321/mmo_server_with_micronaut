package server.skills.available.mage.nature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Single;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.ActorStatus;
import server.combat.model.CombatData;
import server.common.dto.Location;
import server.skills.active.aoe.TickingAoeSkill;
import server.skills.model.SkillDependencies;
import server.skills.model.SkillTarget;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

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
                Map.of(),
                600,
                2500,
                4,
                false);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {}

    @Override
    public boolean canApply(CombatData combatData, SkillTarget skillTarget) {
        return true;
    }

    private Single<List<String>> getAffectedActors(SkillTarget skillTarget, CombatData combatData) {
        return actorMotionRepository
                .fetchActorMotion(skillTarget.getTargetId())
                .flatMap(motion -> getAffectedActors(new Location(motion), combatData.getActorId()));
    }

    private Consumer<SkillDependencies> applyMoonsVengeance() {
        return (data) -> {
            Stats actorStats = data.getActorStats();
            Stats targetStats = data.getTargetStats();
            ActorStatus actorStatus = data.getActorStatus();
            ActorStatus targetStatus = data.getTargetStatus();

            SkillTarget skillTarget = data.getSkillTarget();
            CombatData combatData = data.getCombatData();

            Map<String, Double> actorDerived = actorStats.getDerivedStats();

            Double mgcAmp = actorDerived.getOrDefault(StatsTypes.MAG_AMP.getType(), 1.0);
            Double dmgAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());
            dmgAmt = dmgAmt * mgcAmp * (1 + rand.nextDouble(0.15));

            Map<String, Double> damageMap = Map.of(DamageTypes.MAGIC.getType(), dmgAmt);

            getAffectedActors(skillTarget, combatData)
                    .doOnSuccess(actors -> actors.stream().parallel().forEach(actor ->
                            statsService.getStatsFor(actor).doOnSuccess(target ->
                                    statsService.takeDamage(target, damageMap, actorStats)).subscribe()))
                    .doOnError(err -> log.error("Error applying moons vengeance, {}", err.getMessage()))
                    .subscribe();
        };
    }

    @Override
    public void applyEffect(CombatData combatData, SkillTarget skillTarget) {
        prepareApply(combatData, skillTarget, applyMoonsVengeance());
    }
}
