package server.attribute.talents.available.magic.mage.arcanist.tier3;

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
@JsonTypeName("Greater arcane shielding")
@EqualsAndHashCode(callSuper = false)
public class GreaterArcaneShielding extends Talent {

    public GreaterArcaneShielding() {
        this.name = "Greater arcane shielding";
        this.description = "Extensive knowledge of the arcane allows you to reduce physical and magical damage taken by 1% per rank (max 5 ranks)";
        this.levels = 5;
        this.treeName = "Arcanist";
        this.talentType = TalentType.PASSIVE.getType();
        this.tier = 1;
        this.applyType = AttributeApplyType.DERIVED_STATS.getType();

        this.attributeEffects = List.of(
                new AttributeEffects(StatsTypes.PHY_REDUCTION.getType(), 0.01, null),
                new AttributeEffects(StatsTypes.MGC_REDUCTION.getType(), 0.01, null));

        AttributeRequirements attributeRequirements = new AttributeRequirements();
        attributeRequirements.setRequirements(Map.of(ClassTypes.MAGE.getType(), 5));
        attributeRequirements.setDependencies(List.of());

        this.attributeRequirements = attributeRequirements;
    }
}
