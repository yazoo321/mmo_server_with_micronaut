package server.skills.available.mage.nature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Stunned;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

@Serdeable
@JsonTypeName("Vine grab")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class VineGrab extends ChannelledSkill {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public VineGrab() {
        super(
                "Vine Grab",
                "Spawn vines which will grab your opponent and lock them into place",
                Map.of(StatsTypes.MAGIC_DAMAGE.getType(), 30.0),
                0,
                1000,
                false,
                true,
                1000,
                500,
                Map.of(),
                0,
                0);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        combatData = skillDependencies.getCombatData();
        Double dmgAmt = derived.get(StatsTypes.MAGIC_DAMAGE.getType());

        // TODO: introduce new magic physical type, so it scales with magic amp
        Map<String, Double> damageMap = Map.of(DamageTypes.MAGIC.getType(), dmgAmt);

        requestTakeDamage(combatData.getActorId(), skillTarget.getTargetId(), damageMap);
        applyStunEffect(combatData, skillTarget.getTargetId());
    }

    private void applyStunEffect(CombatData combatData, String actorId) {
        Status stun =
                new Stunned(
                        Instant.now().plusMillis(2000), combatData.getActorId(), this.getName());
        requestAddStatusEffect(actorId, Set.of(stun));
    }
}
