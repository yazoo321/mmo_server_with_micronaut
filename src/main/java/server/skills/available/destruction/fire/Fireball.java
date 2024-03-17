package server.skills.available.destruction.fire;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import lombok.EqualsAndHashCode;
import server.attribute.stats.model.Stats;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

@Serdeable
@JsonTypeName("Fireball")
@EqualsAndHashCode(callSuper = false)
public class Fireball extends ChannelledSkill {

    public Fireball() {
        super(
                "Fireball",
                "Hurl a fireball at a selected target",
                Map.of(StatsTypes.MAGIC_DAMAGE.getType(), 300.0),
                0,
                1500,
                false,
                true,
                1000,
                1000,
                Map.of());
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        Map<String, Double> actorDerived = combatData.getDerivedStats();
        Double healAmp = actorDerived.getOrDefault(StatsTypes.MAG_AMP.getType(), 1.0);

        Double dmgAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());
        dmgAmt = dmgAmt * healAmp * (1 + rand.nextDouble(0.15));

        Map<DamageTypes, Double> damageMap = Map.of(DamageTypes.MAGIC, dmgAmt);

        String target = skillTarget.getTargetId();
        Stats targetStats = statsService.getStatsFor(target).blockingGet();

        Stats stats = statsService.takeDamage(targetStats, damageMap);

        checkDeath(stats);
    }

    @Override
    public boolean canApply(CombatData combatData, SkillTarget skillTarget) {
        return true;
    }
}
