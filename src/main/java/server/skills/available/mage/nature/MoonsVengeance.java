package server.skills.available.mage.nature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Single;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.combat.model.CombatData;
import server.common.dto.Location;
import server.skills.active.aoe.TickingAoeSkill;
import server.skills.model.SkillTarget;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

    private Single<List<String>> getAffectedActors(SkillTarget skillTarget, CombatData combatData) {
        return actorMotionRepository
                .fetchActorMotion(skillTarget.getTargetId())
                .flatMap(motion -> getAffectedActors(new Location(motion), combatData.getActorId()));
    }

    @Override
    public void applyEffect() {
        SkillTarget skillTarget = skillDependencies.getSkillTarget();
        CombatData combatData = skillDependencies.getCombatData();

        Double dmgAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());

        Map<String, Double> damageMap = Map.of(DamageTypes.MAGIC.getType(), dmgAmt);

        getAffectedActors(skillTarget, combatData).doOnSuccess(
                targets -> targets.stream().parallel().forEach(target ->
                        requestTakeDamage(combatData.getActorId(), target, damageMap)))
                .doOnError(err -> log.error("Error applying moons vengeance, {}", err.getMessage()))
                .subscribe();

    }
}
