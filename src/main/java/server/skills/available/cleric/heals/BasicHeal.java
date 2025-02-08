package server.skills.available.cleric.heals;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.DamageTypes;
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
                Map.of(DamageTypes.POSITIVE.getType(), -100.0),
                1000,
                1000,
                false,
                true,
                500,
                0,
                Map.of(ClassTypes.CLERIC.getType(), 1),
                0,
                0);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        requestTakeDamage(combatData.getActorId(), skillTarget.getTargetId(), derived);
    }
}
