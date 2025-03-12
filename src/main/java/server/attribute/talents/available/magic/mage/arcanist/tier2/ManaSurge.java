package server.attribute.talents.available.magic.mage.arcanist.tier2;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeApplyType;
import server.attribute.common.model.AttributeRequirements;
import server.attribute.stats.model.DamageSource;
import server.attribute.stats.model.Stats;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentType;
import server.attribute.talents.service.TalentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Serdeable
@JsonTypeName("Mana surge")
@EqualsAndHashCode(callSuper = false)
public class ManaSurge extends Talent {

    public ManaSurge() {
        this.name = "Mana surge";
        this.description = "Critical hits restore 2% of max mana per rank (max 5 ranks)";
        this.levels = 5;
        this.treeName = "Arcanist";
        this.talentType = TalentType.AUGMENT.getType();
        this.tier = 2;
        this.attributeEffects = List.of();
        this.applyType = AttributeApplyType.ON_CRIT_APPLY.getType();

        AttributeRequirements attributeRequirements = new AttributeRequirements();
        attributeRequirements.setRequirements(Map.of(ClassTypes.MAGE.getType(), 3));
        attributeRequirements.setDependencies(List.of());

        this.attributeRequirements = attributeRequirements;
    }

    @Override
    public void applyEffect(
            Integer level, TalentService talentService, Stats actorStats, Stats targetStats) {
        // TODO: Need to re-work the damage apply and check death functions, break them into
        // multiple services
        DamageSource damageSource = new DamageSource();
        damageSource.setActorId(actorStats.getActorId());
        double increase = 0.02 * level;
        Map<String, Double> damageMap = new HashMap<>();
        damageMap.put("MP", increase);

        damageSource.setDamageMap(damageMap);
        damageSource.setAdditionalData("PERCENT");

        talentService.requestStatChange(damageSource);
    }
}
