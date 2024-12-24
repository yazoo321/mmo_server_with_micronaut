package server.skills.available.destruction.fire;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import java.util.function.Consumer;

import io.reactivex.rxjava3.core.Single;
import lombok.EqualsAndHashCode;
import server.attribute.stats.model.Stats;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.ActorStatus;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillDependencies;
import server.skills.model.SkillTarget;

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
        prepareApply(combatData, skillTarget, applyFireball());
    }

    private Consumer<SkillDependencies> applyFireball() {
        return (data) -> {
            Stats actorStats = data.getActorStats();
            Stats targetStats = data.getTargetStats();
            ActorStatus actorStatus = data.getActorStatus();
            ActorStatus targetStatus = data.getTargetStatus();

            if (!actorStatus.canCast()) {

            }

            Map<String, Double> actorDerived = actorStats.getDerivedStats();
            Double mgcAmp = actorDerived.getOrDefault(StatsTypes.MAG_AMP.getType(), 1.0);
            Double dmgAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());
            dmgAmt = dmgAmt * mgcAmp * (1 + rand.nextDouble(0.15));

            Map<String, Double> damageMap = Map.of(DamageTypes.FIRE.getType(), dmgAmt);

            targetStats = statsService.takeDamage(targetStats, damageMap, actorStats);

        };
    }

    private void prepareApply(CombatData combatData, SkillTarget skillTarget, Consumer<SkillDependencies> skillConsumer) {
        Single<Stats> actorStatsSingle = statsService.getStatsFor(combatData.getActorId());
        Single<Stats> targetStatsSingle = statsService.getStatsFor(skillTarget.getTargetId());
        Single<ActorStatus> actorStatusSingle = statusService.getActorStatus(combatData.getActorId());
        Single<ActorStatus> targetStatusSingle = statusService.getActorStatus(skillTarget.getTargetId());

        Single.zip(actorStatsSingle, targetStatsSingle, actorStatusSingle, targetStatusSingle,
                (actorStats, targetStats, actorStatus, targetStatus) -> {
            SkillDependencies dependencies = SkillDependencies.builder()
                    .actorStats(actorStats)
                            .targetStats(targetStats)
                                    .actorStatus(actorStatus)
                                            .targetStatus(targetStatus).build();
            skillConsumer.accept(dependencies);
            return true;
        }).subscribe();
    }

    @Override
    public boolean canApply(CombatData combatData, SkillTarget skillTarget) {
        return true;
    }
}
