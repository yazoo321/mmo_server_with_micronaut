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
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Serdeable
@JsonTypeName("Eclipse burst")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class EclipseBurst extends ChannelledSkill {

    public EclipseBurst() {
        super(
                "Eclipse Burst",
                "Summon the moon to fiercely strike a target with its magic power",
                Map.of(StatsTypes.MAGIC_DAMAGE.getType(), 80.0),
                0,
                1000,
                false,
                true,
                1000,
                0,
                Map.of(), 0, 0);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        String target = skillTarget.getTargetId();

        Single<Stats> singleActorStats = statsService.getStatsFor(combatData.getActorId());
        Single<Stats> singleTargetStats = statsService.getStatsFor(target);


        Single.zip(singleActorStats, singleTargetStats, (actorStats, targetStats) -> {
            Map<String, Double> actorDerived = actorStats.getDerivedStats();
            Double mgcAmp = actorDerived.getOrDefault(StatsTypes.MAG_AMP.getType(), 1.0);

            Double dmgAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());
            dmgAmt = dmgAmt * mgcAmp * (1 + rand.nextDouble(0.15));

            Map<DamageTypes, Double> damageMap = Map.of(DamageTypes.MAGIC, dmgAmt);

            Stats stats = statsService.takeDamage(targetStats, damageMap, combatData.getActorId());

            checkDeath(stats, combatData.getActorId());
            return true;
        }).subscribe();


    }

    @Override
    public boolean canApply(CombatData combatData, SkillTarget skillTarget) {
        return true;
    }

}
