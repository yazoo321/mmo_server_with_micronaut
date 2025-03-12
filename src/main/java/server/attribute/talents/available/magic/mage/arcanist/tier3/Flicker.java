package server.attribute.talents.available.magic.mage.arcanist.tier3;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeApplyType;
import server.attribute.common.model.AttributeRequirements;
import server.attribute.stats.model.DamageSource;
import server.attribute.stats.model.Stats;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.AttributeMod;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentType;
import server.attribute.talents.service.TalentService;

import java.time.Instant;
import java.util.*;

@Slf4j
@Serdeable
@JsonTypeName("Flicker")
@EqualsAndHashCode(callSuper = false)
public class Flicker extends Talent {
    private final Random rand = new Random();

    public Flicker() {
        this.name = "Flicker";
        this.description = "Taking damage has a chance (5%) to reduce physical and magic damage taken by 10% for 5 sec. (Max 3 ranks)";
        this.levels = 3;
        this.treeName = "Arcanist";
        this.talentType = TalentType.AUGMENT.getType();
        this.tier = 2;
        this.attributeEffects = List.of();
        this.applyType = AttributeApplyType.ON_HIT_CONSUME.getType();

        AttributeRequirements attributeRequirements = new AttributeRequirements();
        attributeRequirements.setRequirements(Map.of(ClassTypes.MAGE.getType(), 5));
        attributeRequirements.setDependencies(List.of());

        this.attributeRequirements = attributeRequirements;
    }

    @Override
    public void applyEffect(
            Integer level, TalentService talentService, Stats actorStats, Stats targetStats) {

        double roll = rand.nextDouble(100.0);
        if (roll > 5) {
            return;
        }

        Instant expire = Instant.now().plusMillis(5_000);
        String sourceActor = actorStats.getActorId();

        Double reduction = 0.1 * level;

        Status phyReduction =
                new AttributeMod(
                        expire, sourceActor, StatsTypes.PHY_REDUCTION, reduction, 0.0, 1, this.name);
        Status mgcReduction =
                new AttributeMod(
                        expire, sourceActor, StatsTypes.MGC_REDUCTION, reduction, 0.0, 1, this.name);

        ActorStatus actorStatus = new ActorStatus();
        actorStatus.setActorId(actorStats.getActorId());
        actorStatus.setActorStatuses(Set.of(phyReduction, mgcReduction));

        talentService.requestAddStatusToActor(actorStatus);
    }
}
