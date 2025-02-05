package server.skills.available.mage.nature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Stunned;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Serdeable
@JsonTypeName("Vine grab")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class VineGrab extends ChannelledSkill {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public VineGrab() {
        super(
                "Vine Grab",
                "Spawn vines which will grab your opponent and lock them into place",
                Map.of(StatsTypes.MAGIC_DAMAGE.getType(), 30.0),
                0,
                1000,
                false,
                true,
                1000,
                500,
                Map.of(),
                0,
                0);
    }


    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        Stats actorStats = skillDependencies.getActorStats();
        Stats targetStats = skillDependencies.getTargetStats();
        ActorStatus actorStatus = skillDependencies.getActorStatus();
        ActorStatus targetStatus = skillDependencies.getTargetStatus();
        combatData = skillDependencies.getCombatData();

        Map<String, Double> actorDerived = actorStats.getDerivedStats();
        Double mgcAmp =
                actorDerived.getOrDefault(StatsTypes.MAG_AMP.getType(), 1.0);

        Double dmgAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());
        dmgAmt = dmgAmt * mgcAmp * (1 + rand.nextDouble(0.15));

        Map<String, Double> damageMap =
                Map.of(DamageTypes.PHYSICAL.getType(), dmgAmt);

        statsService.takeDamage(targetStats, damageMap, actorStats);
        applyStunEffect(combatData, targetStatus);
    }

    private void applyStunEffect(CombatData combatData, ActorStatus targetStatus) {
        Status tangled =
                new Stunned(Instant.now().plusSeconds((long) 2.0), combatData.getActorId(), this.getName());
        statusService.addStatusToActor(targetStatus, Set.of(tangled));

        // TODO: check if we can remove this, may not be necessary - we can increase resolution of global status checker
        scheduler.schedule(
                () -> statusService.removeStatusFromActor(targetStatus, Set.of(tangled)),
                2000,
                TimeUnit.MILLISECONDS);
    }
}
