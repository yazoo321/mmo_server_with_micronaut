package server.attribute.talents.available.melee.fighter.weaponmaster.tier1;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeApplyType;
import server.attribute.common.model.AttributeEffects;
import server.attribute.common.model.AttributeRequirements;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentType;

@Slf4j
@Serdeable
@JsonTypeName("Sharpened blades")
@EqualsAndHashCode(callSuper = false)
public class SharpenedBlades extends Talent {

    public SharpenedBlades() {
        this.name = "Sharpened blades";
        this.description = "Increases your physical critical hit chance.";
        this.levels = 5;
        this.treeName = "Weaponmaster";
        this.talentType = TalentType.PASSIVE.getType();
        this.tier = 1;
        this.applyType = AttributeApplyType.DERIVED_STATS.getType();

        AttributeEffects attributeEffect =
                new AttributeEffects(StatsTypes.PHY_CRIT.getType(), 2.0, null);

        this.attributeEffects = List.of(attributeEffect);

        AttributeRequirements attributeRequirements = new AttributeRequirements();
        attributeRequirements.setRequirements(Map.of(ClassTypes.FIGHTER.getType(), 1));
        attributeRequirements.setDependencies(List.of());

        this.attributeRequirements = attributeRequirements;
    }
}
