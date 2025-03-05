package server.skills.available.mage.nature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.DamageTypes;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

@Serdeable
@JsonTypeName("Sun smite")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class SunSmite extends ChannelledSkill {

    public SunSmite() {
        super(
                "Sun smite",
                "Summon the sun to smite the foe with fire damage",
                Map.of(DamageTypes.FIRE.getType(), 100.0),
                0,
                1000,
                false,
                true,
                1000,
                0,
                Map.of(ClassTypes.MAGE.getType(), 3),
                0,
                0);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        requestTakeDamage(combatData.getActorId(), skillTarget.getTargetId(), derived);
    }
}
