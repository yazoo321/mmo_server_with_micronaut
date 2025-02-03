package server.attribute.talents.available.melee.fighter.weaponmaster.tier3;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeApplyType;
import server.attribute.common.model.AttributeEffects;
import server.attribute.common.model.AttributeRequirements;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentType;

import java.util.List;
import java.util.Map;

@Slf4j
@Serdeable
@JsonTypeName("Iron grip")
@EqualsAndHashCode(callSuper = false)
public class IronGrip extends Talent {

    public IronGrip() {
        this.name = "Iron grip";
        this.description = "Increases your physical damage by 4% per rank.";
        this.levels = 5;
        this.treeName = "Weaponmaster";
        this.talentType = TalentType.PASSIVE.getType();
        this.tier = 1;
        this.applyType = AttributeApplyType.DERIVED_STATS.getType();

        AttributeEffects attributeEffect =
                new AttributeEffects(StatsTypes.PHY_AMP.getType(), 0.04, null);

        this.attributeEffects = List.of(attributeEffect);

        AttributeRequirements attributeRequirements = new AttributeRequirements();
        attributeRequirements.setRequirements(Map.of(ClassTypes.FIGHTER.getType(), 1));
        attributeRequirements.setDependencies(Map.of());

        this.attributeRequirements = attributeRequirements;
    }
}
