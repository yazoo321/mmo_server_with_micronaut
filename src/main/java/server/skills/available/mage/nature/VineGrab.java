package server.skills.available.mage.nature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.DamageTypes;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Stunned;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Serdeable
@JsonTypeName("Vine grab")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class VineGrab extends ChannelledSkill {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public VineGrab() {
        super(
                "Vine grab",
                "Spawn vines which will grab your opponent and lock them into place for 2 seconds",
                Map.of(DamageTypes.MAGIC.getType(), 30.0),
                0,
                1000,
                false,
                true,
                1000,
                500,
                Map.of(ClassTypes.MAGE.getType(), 4),
                2_000,
                0);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        combatData = skillDependencies.getCombatData();
        // TODO: introduce new magic physical type, so it scales with magic amp

        requestTakeDamage(combatData.getActorId(), skillTarget.getTargetId(), derived);
        applyStunEffect(combatData, skillTarget.getTargetId());
    }

    private void applyStunEffect(CombatData combatData, String actorId) {
        // TODO: rather than stun, it should set the movement to 0
        Status stun =
                new Stunned(
                        Instant.now().plusMillis(durationMs), combatData.getActorId(), this.getName());
        requestAddStatusEffect(actorId, Set.of(stun));
    }
}
