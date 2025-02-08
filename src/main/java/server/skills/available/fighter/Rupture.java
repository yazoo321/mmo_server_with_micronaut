package server.skills.available.fighter;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.DamageTypes;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Bleeding;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Slf4j
@Serdeable
@JsonTypeName("Rupture")
@EqualsAndHashCode(callSuper = false)
public class Rupture extends ChannelledSkill {

    public Rupture() {
        super(
                "Rupture",
                "Rupture the target, causing bleed damage over 10 seconds",
                Map.of(DamageTypes.BLEEDING.getType(), 100.0),
                0,
                50,
                true,
                true,
                250,
                0,
                Map.of(ClassTypes.FIGHTER.getType(), 1),
                10_000,
                10);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        requestTakeDamage(combatData.getActorId(), skillTarget.getTargetId(), derived);
        addBleedEffect(combatData, skillTarget);
    }

    private void addBleedEffect(CombatData combatData, SkillTarget skillTarget) {
        // add burning effect
        Instant duration = Instant.now().plusMillis(durationMs);
        Status bleed = new Bleeding(duration, combatData.getActorId(), derived.get(DamageTypes.BLEEDING.getType()), 1, this.getName());
        requestAddStatusEffect(skillTarget.getTargetId(), Set.of(bleed));
    }
}
