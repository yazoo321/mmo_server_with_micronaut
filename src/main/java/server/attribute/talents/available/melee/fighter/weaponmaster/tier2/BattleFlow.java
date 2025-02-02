package server.attribute.talents.available.melee.fighter.weaponmaster.tier2;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeApplyType;
import server.attribute.common.model.AttributeRequirements;
import server.attribute.stats.model.Stats;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentType;
import server.attribute.talents.service.TalentService;

import java.util.List;
import java.util.Map;

@Slf4j
@Serdeable
@JsonTypeName("Battle flow")
@EqualsAndHashCode(callSuper = false)
public class BattleFlow extends Talent {

    public BattleFlow() {
        this.name = "Battle flow";
        this.description = "Killing an enemy restores 3% HP & Mana per rank (max 5 ranks)";
        this.levels = 3;
        this.treeName = "Weaponmaster";
        this.talentType = TalentType.AUGMENT.getType();
        this.tier = 2;
        this.attributeEffects = List.of();
        this.applyType = AttributeApplyType.ON_DEATH_APPLY.getType();

        AttributeRequirements attributeRequirements = new AttributeRequirements();
        attributeRequirements.setRequirements(Map.of(ClassTypes.FIGHTER.getType(), 1));
        attributeRequirements.setDependencies(Map.of());

        this.attributeRequirements = attributeRequirements;
    }

    @Override
    public void applyEffect(
            Integer level, TalentService talentService, Stats actorStats, Stats targetStats) {
        // TODO: Need to re-work the damage apply and check death functions, break them into multiple services
    }
}
