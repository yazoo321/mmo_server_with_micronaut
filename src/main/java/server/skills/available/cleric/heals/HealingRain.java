package server.skills.available.cleric.heals;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.DamageTypes;
import server.combat.model.CombatData;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.skills.active.aoe.TickingAoeSkill;
import server.skills.model.SkillTarget;

@Getter
@JsonTypeName("Healing rain")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class HealingRain extends TickingAoeSkill {

    public HealingRain() {
        super(
                "Healing rain",
                "Channel an aura of tranquility which periodically heals actors nearby",
                Map.of(DamageTypes.POSITIVE.getType(), -20.0),
                1000,
                3000,
                false,
                true,
                500,
                0,
                Map.of(ClassTypes.CLERIC.getType(), 3),
                600,
                3000,
                10,
                true);
    }

    @Override
    public void endSkill(CombatData combatData, SkillTarget skillTarget) {
        // do nothing
    }

    @Override
    public void applyEffect() {
        CombatData combatData = skillDependencies.getCombatData();
        SkillTarget skillTarget = skillDependencies.getSkillTarget();
        Motion actorMotion = skillDependencies.getActorMotion();
        skillTarget.setLocation(new Location(actorMotion));

        getAffectedActors(skillTarget)
                .doOnSuccess(
                        targets ->
                                requestTakeDamageToMultipleActors(combatData.getActorId(), targets))
                .doOnError(err -> log.error("Error applying moons vengeance, {}", err.getMessage()))
                .subscribe();
    }
}
