package server.skills.available.mage.fire;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Burning;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillDependencies;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Serdeable
@JsonTypeName("Fireball")
@EqualsAndHashCode(callSuper = false)
public class Fireball extends ChannelledSkill {

    public Fireball() {
        super(
                "Fireball",
                "Hurl a fireball at a selected target",
                Map.of(StatsTypes.MAGIC_DAMAGE.getType(), 80.0),
                0,
                1500,
                false,
                true,
                1000,
                500,
                Map.of(), 0, 0);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        prepareApply(combatData, skillTarget, applyEffect());
    }

    private Consumer<SkillDependencies> applyEffect() {
        return (data) -> {
            Stats actorStats = data.getActorStats();
            Stats targetStats = data.getTargetStats();
            ActorStatus actorStatus = data.getActorStatus();
            ActorStatus targetStatus = data.getTargetStatus();

            Map<String, Double> actorDerived = actorStats.getDerivedStats();
            Double mgcAmp = actorDerived.getOrDefault(StatsTypes.MAG_AMP.getType(), 1.0);
            Double dmgAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());
            dmgAmt = dmgAmt * mgcAmp * (1 + rand.nextDouble(0.15));

            Map<String, Double> damageMap = Map.of(DamageTypes.FIRE.getType(), dmgAmt);

            statsService.takeDamage(targetStats, damageMap, actorStats);

            // add burning effect
            Instant duration = Instant.now().plusMillis(1500);
            Double tickDamage = dmgAmt / 7;
            Status burn = new Burning(duration, actorStats.getActorId(), tickDamage, 1, this.getName());

            statusService.addStatusToActor(targetStatus, Set.of(burn));
        };
    }

    @Override
    public boolean canApply(CombatData combatData, SkillTarget skillTarget) {
        return true;
    }
}
