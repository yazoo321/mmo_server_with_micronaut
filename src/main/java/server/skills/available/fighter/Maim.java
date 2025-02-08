package server.skills.available.fighter;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.AttributeMod;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Slf4j
@Serdeable
@JsonTypeName("Maim")
@EqualsAndHashCode(callSuper = false)
public class Maim extends ChannelledSkill {

    public Maim() {
        super(
                "Maim",
                "Main the target, inflicting minor damage and slowing them for 7 seconds by 40%",
                Map.of(DamageTypes.PHYSICAL.getType(), 20.0),
                0,
                50,
                true,
                true,
                250,
                0,
                Map.of(ClassTypes.FIGHTER.getType(), 1),
                7_000,
                0);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        requestTakeDamage(combatData.getActorId(), skillTarget.getTargetId(), derived);
        addSlowingEffect(combatData, skillTarget);
    }

    private void addSlowingEffect(CombatData combatData, SkillTarget skillTarget) {
        // add burning effect
        Instant duration = Instant.now().plusMillis(durationMs);
        Status slow = new AttributeMod(duration, combatData.getActorId(), StatsTypes.MOVE_SPEED, 0.0, 0.6, 1, this.getName());
        requestAddStatusEffect(skillTarget.getTargetId(), Set.of(slow));
    }
}
