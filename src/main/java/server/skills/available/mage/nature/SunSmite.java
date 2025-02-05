package server.skills.available.mage.nature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.ActorStatus;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillDependencies;
import server.skills.model.SkillTarget;

import java.util.Map;
import java.util.function.Consumer;

@Serdeable
@JsonTypeName("Sun smite")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class SunSmite extends ChannelledSkill {

    public SunSmite() {
        super(
                "Sun smite",
                "Summon the sun to smite the foe with fire damage",
                Map.of(StatsTypes.MAGIC_DAMAGE.getType(), 100.0),
                0,
                1000,
                false,
                true,
                1000,
                0,
                Map.of(), 0, 0);
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

            Map<String, Double> damageMap = Map.of(DamageTypes.MAGIC.getType(), dmgAmt);

            statsService.takeDamage(targetStats, damageMap, actorStats);
        };
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        prepareApply(combatData, skillTarget, applyEffect());
    }

    @Override
    public boolean canApply(CombatData combatData, SkillTarget skillTarget) {
        return true;
    }

}
