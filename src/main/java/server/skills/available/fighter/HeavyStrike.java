package server.skills.available.fighter;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

@Slf4j
@Serdeable
@JsonTypeName("Heavy strike")
@EqualsAndHashCode(callSuper = false)
public class HeavyStrike extends ChannelledSkill {

    public HeavyStrike() {
        super(
                "Heavy strike",
                "Instantly strike the target with your main-hand weapon, dealing 150% weapon"
                        + " damage",
                Map.of(),
                5000,
                50,
                true,
                true,
                250,
                0,
                Map.of(ClassTypes.FIGHTER.getType(), 1),
                0,
                0);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        Double dmg =
                getSkillDependencies()
                        .getActorStats()
                        .getDerived(StatsTypes.MAINHAND_WEAPON_DAMAGE);
        dmg = dmg * 1.5;
        Map<String, Double> damageMap = Map.of(DamageTypes.PHYSICAL.getType(), dmg);
        requestTakeDamage(combatData.getActorId(), skillTarget.getTargetId(), damageMap);
    }
}
