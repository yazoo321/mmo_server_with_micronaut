package server.skills.available.mage.nature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.DamageTypes;
import server.combat.model.CombatData;
import server.skills.active.aoe.TickingAoeSkill;
import server.skills.model.SkillTarget;

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
                Map.of(DamageTypes.MAGIC.getType(), 50.0),
                0,
                2000,
                false,
                true,
                1000,
                500,
                Map.of(ClassTypes.MAGE.getType(), 3),
                600,
                2500,
                4,
                false);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {}

    @Override
    public void applyEffect() {
        SkillTarget skillTarget = skillDependencies.getSkillTarget();
        CombatData combatData = skillDependencies.getCombatData();

        getAffectedActors(skillTarget)
                .doOnSuccess(targets ->
                        requestTakeDamageToMultipleActors(combatData.getActorId(), targets))
                .doOnError(err -> log.error("Error applying moons vengeance, {}", err.getMessage()))
                .subscribe();
    }
}
