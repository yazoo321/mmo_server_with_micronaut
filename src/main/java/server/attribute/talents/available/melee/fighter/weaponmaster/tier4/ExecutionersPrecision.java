package server.attribute.talents.available.melee.fighter.weaponmaster.tier4;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeApplyType;
import server.attribute.common.model.AttributeRequirements;
import server.attribute.stats.model.Stats;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.AttributeMod;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentType;
import server.attribute.talents.service.TalentService;

@Slf4j
@Serdeable
@JsonTypeName("Executioners precision")
@EqualsAndHashCode(callSuper = false)
public class ExecutionersPrecision extends Talent {

    private Random rand = new Random();

    public ExecutionersPrecision() {
        this.name = "Executioners precision";
        this.description =
                " Attacks against low-health enemies (below 30%) will have a 50% chance to increase"
                        + " physical damage by 5% (per rank) for 5 seconds (max 5 ranks).";
        this.levels = 5;
        this.treeName = "Weaponmaster";
        this.talentType = TalentType.AUGMENT.getType();
        this.tier = 4;
        this.attributeEffects = List.of();
        this.applyType = AttributeApplyType.ON_HIT_APPLY.getType();

        AttributeRequirements attributeRequirements = new AttributeRequirements();
        attributeRequirements.setRequirements(Map.of(ClassTypes.FIGHTER.getType(), 8));
        attributeRequirements.setDependencies(List.of());

        this.attributeRequirements = attributeRequirements;
    }

    @Override
    public void applyEffect(
            Integer level, TalentService talentService, Stats actorStats, Stats targetStats) {

        if (targetStats.getDerived(StatsTypes.CURRENT_HP)
                        / targetStats.getDerived(StatsTypes.MAX_HP)
                > 0.3) {
            // the target has more than 30% HP
            return;
        }
        double roll = rand.nextDouble(100.0);
        if (roll > 50) {
            return;
        }

        Instant expire = Instant.now().plusMillis(5_000);
        String sourceActor = actorStats.getActorId();

        Double phyAmpInc = 0.05 * level;

        Status phyAmpStatus =
                new AttributeMod(
                        expire, sourceActor, StatsTypes.PHY_AMP, phyAmpInc, 0.0, 1, this.name);

        ActorStatus actorStatus = new ActorStatus();
        actorStatus.setActorId(actorStats.getActorId());
        actorStatus.setActorStatuses(Set.of(phyAmpStatus));

        talentService.requestAddStatusToActor(actorStatus);
    }
}
