package server.attribute.talents.available.melee.fighter.weaponmaster.tier3;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeApplyType;
import server.attribute.common.model.AttributeRequirements;
import server.attribute.stats.model.Stats;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.ArmorMod;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentType;
import server.attribute.talents.service.TalentService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Serdeable
@JsonTypeName("Sundering strikes")
@EqualsAndHashCode(callSuper = false)
public class SunderingStrikes extends Talent {


    public SunderingStrikes() {
        this.name = "Sundering strikes";
        this.description = "Critical hits reduce enemy armor by 5% for 5 sec (max 3 ranks)";
        this.levels = 3;
        this.treeName = "Weaponmaster";
        this.talentType = TalentType.AUGMENT.getType();
        this.tier = 2;
        this.attributeEffects = List.of();
        this.applyType = AttributeApplyType.ON_CRIT_APPLY.getType();

        AttributeRequirements attributeRequirements = new AttributeRequirements();
        attributeRequirements.setRequirements(Map.of(ClassTypes.FIGHTER.getType(), 1));
        attributeRequirements.setDependencies(Map.of());

        this.attributeRequirements = attributeRequirements;
    }

    @Override
    public void applyEffect(
            Integer level, TalentService talentService, Stats actorStats, Stats targetStats) {

        Instant expire = Instant.now().plusMillis(5000); // lasts for 5 seconds
        String sourceActor = actorStats.getActorId();

        double armorReduction = 0.05 * level;
        armorReduction = 1 - armorReduction;

        Status armorReduce = new ArmorMod(expire, sourceActor, armorReduction, 1, this.name);

        ActorStatus actorStatus = new ActorStatus();
        actorStatus.setActorId(targetStats.getActorId());
        actorStatus.setActorStatuses(Set.of(armorReduce));

        talentService.requestAddStatusToActor(actorStatus);
    }
}
