package server.attribute.talents.available.melee.fighter.weaponmaster.tier2;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeRequirements;
import server.attribute.stats.model.Stats;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentType;

@Slf4j
@Serdeable
@JsonTypeName("Crippling blows")
@EqualsAndHashCode(callSuper = false)
public class CripplingBlows extends Talent {

    public CripplingBlows() {
        this.name = "Crippling blows";
        this.description =
                "Basic attacks have a 10% chance to reduce enemy movement speed by 20% for 3 sec";
        this.levels = 3;
        this.treeName = "Weaponmaster";
        this.talentType = TalentType.AUGMENT.getType();
        this.tier = 2;

        this.attributeEffects = List.of();

        AttributeRequirements attributeRequirements = new AttributeRequirements();
        attributeRequirements.setRequirements(Map.of(ClassTypes.FIGHTER.getType(), 1));
        attributeRequirements.setDependencies(Map.of());

        this.attributeRequirements = attributeRequirements;
    }

    @Override
    public void applyEffect(Stats actorStats, Stats targetStats) {
        // add stats to reduce movement speed
        Double moveSpeed = targetStats.getDerivedStats().get(StatsTypes.MOVE_SPEED.getType());
        if (moveSpeed == null) {
            log.error("Target {} movement speed is null", targetStats.getActorId());
            throw new RuntimeException("Move speed is null");
        }

        targetStats.setDerived(StatsTypes.MOVE_SPEED, moveSpeed * 0.7);
    }
}
