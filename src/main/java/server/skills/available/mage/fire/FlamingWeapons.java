package server.skills.available.mage.fire;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeEffects;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.DamageTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Imbue;
import server.combat.model.CombatData;
import server.skills.active.channelled.ChannelledSkill;
import server.skills.model.SkillTarget;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Slf4j
@Serdeable
@JsonTypeName("Flaming weapons")
@EqualsAndHashCode(callSuper = false)
public class FlamingWeapons extends ChannelledSkill {

    public FlamingWeapons() {
        super(
                "Flaming weapons",
                "Primary and secondary imbue. Granting fire damage and burn effect to all your attacks",
                Map.of(DamageTypes.FIRE.getType(), 40.0),
                0,
                1500,
                false,
                true,
                1000,
                500,
                Map.of(ClassTypes.MAGE.getType(), 10),
                10_000,
                0);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        // add burning effect
        Instant duration = Instant.now().plusMillis(durationMs);
        Double tickDamage = derived.get(DamageTypes.FIRE.getType()) / ticks;


        Map<String, AttributeEffects> primary = Map.of(
                StatsTypes.PRIMARY_IMBUE.getType(),
                new AttributeEffects(DamageTypes.FIRE.getType(), 40.0, 0.0));

        Status primaryImbue = new Imbue(duration, skillTarget.getCasterId(), StatsTypes.PRIMARY_IMBUE, primary, 2, this.getName());

        Map<String, AttributeEffects> secondary = Map.of(
                StatsTypes.PRIMARY_IMBUE.getType(),
                new AttributeEffects(DamageTypes.FIRE.getType(), 40.0, 0.0));

        Status secondaryImbue = new Imbue(duration, skillTarget.getCasterId(), StatsTypes.SECONDARY_IMBUE, secondary, 2, this.getName());

        requestAddStatusEffect(skillTarget.getTargetId(), Set.of(primaryImbue));    }

    private void addBurningEffect(CombatData combatData, SkillTarget skillTarget) {

    }
}
