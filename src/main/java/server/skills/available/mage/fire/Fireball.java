package server.skills.available.mage.fire;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.DamageTypes;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Burning;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Slf4j
@Serdeable
@JsonTypeName("Fireball")
@EqualsAndHashCode(callSuper = false)
public class Fireball extends ChannelledSkill {

    public Fireball() {
        super(
                "Fireball",
                "Hurl a fireball at a selected target",
                Map.of(DamageTypes.FIRE.getType(), 80.0),
                0,
                1500,
                false,
                true,
                1000,
                500,
                Map.of(ClassTypes.MAGE.getType(), 1),
                0,
                0);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {

        requestTakeDamage(combatData.getActorId(), skillTarget.getTargetId(), derived);
        addBurningEffect(combatData, skillTarget);
    }

    private void addBurningEffect(CombatData combatData, SkillTarget skillTarget) {
        // add burning effect
        Instant duration = Instant.now().plusMillis(1500);
        Double tickDamage = derived.get(DamageTypes.FIRE.getType()) / 7;
        Status burn = new Burning(duration, combatData.getActorId(), tickDamage, 1, this.getName());

        requestAddStatusEffect(skillTarget.getTargetId(), Set.of(burn));
    }
}
