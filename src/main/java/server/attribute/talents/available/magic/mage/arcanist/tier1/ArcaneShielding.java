package server.attribute.talents.available.magic.mage.arcanist.tier1;

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
@JsonTypeName("Arcane shielding")
@EqualsAndHashCode(callSuper = false)
public class ArcaneShielding extends Talent {

    public ArcaneShielding() {
        this.name = "Arcane shielding";
        this.description = " Increases magic resistance by 2% per rank (max 5 ranks)";
        this.levels = 5;
        this.treeName = "Arcanist";
        this.talentType = TalentType.PASSIVE.getType();
        this.tier = 1;
        this.applyType = AttributeApplyType.DERIVED_STATS.getType();

        AttributeEffects attributeEffect =
                new AttributeEffects(StatsTypes.MGC_REDUCTION.getType(), 0.02, null);

        this.attributeEffects = List.of(attributeEffect);

        AttributeRequirements attributeRequirements = new AttributeRequirements();
        attributeRequirements.setRequirements(Map.of(ClassTypes.MAGE.getType(), 1));
        attributeRequirements.setDependencies(List.of());

        this.attributeRequirements = attributeRequirements;
    }
}
