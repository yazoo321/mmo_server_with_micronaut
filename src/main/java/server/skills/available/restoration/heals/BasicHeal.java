package server.skills.available.restoration.heals;

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
                Map.of(
                        StatsTypes.MAGIC_DAMAGE.getType(), -100.0
                ),
                1000,
                1000,
                false,
                500,
                Map.of()
        );
    }

    @Override
    public void startSkill(CombatData combatData, SkillTarget skillTarget) {
        Map<String, Double> derived = combatData.getDerivedStats();

        Double healAmp = derived.getOrDefault(StatsTypes.MAG_AMP.getType(), 1.0);

        Double healAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());
        healAmt = healAmt * healAmp * (1 + rand.nextDouble(0.15));

        Map<DamageTypes, Double> damageMap = Map.of(DamageTypes.POSITIVE, healAmt);

        String target = skillTarget.getTargetId();

        Stats targetStats = statsService.getStatsFor(target).blockingGet();

        statsService.takeDamage(targetStats, damageMap);
    }

    @Override
    public boolean canApply(CombatData combatData, SkillTarget skillTarget) {
        return true;
    }
}
