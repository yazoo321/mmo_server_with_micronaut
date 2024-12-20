package server.skills.available.destruction.nature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
        String target = skillTarget.getTargetId();

        Single<Stats> singleActorStats = statsService.getStatsFor(combatData.getActorId());
        Single<Stats> singleTargetStats = statsService.getStatsFor(target);
        Single<ActorStatus> singleTargetStatus = statusService.getActorStatus(target);

        Single.zip(
                        singleActorStats,
                        singleTargetStats,
                        singleTargetStatus,
                        (actorStats, targetStats, targetStatus) -> {
                            Map<String, Double> actorDerived = actorStats.getDerivedStats();
                            Double mgcAmp =
                                    actorDerived.getOrDefault(StatsTypes.MAG_AMP.getType(), 1.0);

                            Double dmgAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());
                            dmgAmt = dmgAmt * mgcAmp * (1 + rand.nextDouble(0.15));

                            Map<String, Double> damageMap =
                                    Map.of(DamageTypes.PHYSICAL.getType(), dmgAmt);

                            targetStats = statsService.takeDamage(targetStats, damageMap, actorStats);

                            applyStunEffect(combatData, targetStatus);
                            return true;
                        })
                .subscribe();
    }

    @Override
    public boolean canApply(CombatData combatData, SkillTarget skillTarget) {
        return true;
    }

    private void applyStunEffect(CombatData combatData, ActorStatus targetStatus) {
        Status tangled =
                new Stunned(Instant.now().plusSeconds((long) 2.0), combatData.getActorId());
        statusService.addStatusToActor(targetStatus, Set.of(tangled));

        scheduler.schedule(
                () -> statusService.removeStatusFromActor(targetStatus, Set.of(tangled)),
                2000,
                TimeUnit.MILLISECONDS);
    }
}
