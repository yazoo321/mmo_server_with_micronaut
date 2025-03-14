package server.attribute.talents.available.melee.fighter.weaponmaster.tier2;

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
@JsonTypeName("Perfect form")
@EqualsAndHashCode(callSuper = false)
public class PerfectForm extends Talent {

    public PerfectForm() {
        this.name = "Perfect form";
        this.description = "Increases attack speed by 4% per rank";
        this.levels = 5;
        this.treeName = "Weaponmaster";
        this.talentType = TalentType.PASSIVE.getType();
        this.tier = 2;
        this.applyType = AttributeApplyType.DERIVED_STATS.getType();

        AttributeEffects attributeEffect =
                new AttributeEffects(StatsTypes.ATTACK_SPEED.getType(), 4.0, null);

        this.attributeEffects = List.of(attributeEffect);

        AttributeRequirements attributeRequirements = new AttributeRequirements();
        attributeRequirements.setRequirements(Map.of(ClassTypes.FIGHTER.getType(), 3));
        attributeRequirements.setDependencies(List.of());

        this.attributeRequirements = attributeRequirements;
    }
}
