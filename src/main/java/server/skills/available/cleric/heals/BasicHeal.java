package server.skills.available.cleric.heals;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import server.attribute.stats.model.Stats;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

import java.util.Map;

@Getter
@JsonTypeName("Basic heal")
@EqualsAndHashCode(callSuper = false)
public class BasicHeal extends ChannelledSkill {

    public BasicHeal() {
        super(
                "Basic heal",
                "Heal target after a short delay",
                Map.of(StatsTypes.MAGIC_DAMAGE.getType(), -100.0),
                1000,
                1000,
                false,
                true,
                500,
                0,
                Map.of(), 0 , 0 );
    }



    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        Stats actorStats = skillDependencies.getActorStats();
        Map<String, Double> actorDerived = actorStats.getDerivedStats();

        Double healAmp = actorDerived.getOrDefault(StatsTypes.MAG_AMP.getType(), 1.0);

        Double healAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());
        healAmt = healAmt * healAmp * (1 + rand.nextDouble(0.15));

        Map<String, Double> damageMap = Map.of(DamageTypes.POSITIVE.getType(), healAmt);

        String target = skillTarget.getTargetId();

        Stats targetStats = statsService.getStatsFor(target).blockingGet();

        statsService.takeDamage(targetStats, damageMap, actorStats);
    }

}
