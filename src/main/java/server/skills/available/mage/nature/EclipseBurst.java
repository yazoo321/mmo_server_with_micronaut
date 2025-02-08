package server.skills.available.mage.nature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

import java.util.Map;

@Serdeable
@JsonTypeName("Eclipse burst")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class EclipseBurst extends ChannelledSkill {

    public EclipseBurst() {
        super(
                "Eclipse burst",
                "Summon the moon to fiercely strike a target with its magic power",
                Map.of(StatsTypes.MAGIC_DAMAGE.getType(), 80.0),
                4_000,
                1000,
                false,
                true,
                1000,
                0,
                Map.of(ClassTypes.MAGE.getType(), 2),
                0,
                0);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        Double dmgAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());
        Map<String, Double> damageMap = Map.of(DamageTypes.MAGIC.getType(), dmgAmt);
        requestTakeDamage(combatData.getActorId(), skillTarget.getTargetId(), damageMap);
    }
}
