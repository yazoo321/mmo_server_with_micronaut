package server.attribute.talents.available.melee.fighter.weaponmaster.tier2;

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
@JsonTypeName("Crippling blows")
@EqualsAndHashCode(callSuper = false)
public class CripplingBlows extends Talent {

    private final Random rand = new Random();

    public CripplingBlows() {
        this.name = "Crippling blows";
        this.description =
                "Basic attacks have a 20% chance to reduce enemy movement speed by 20% for 3 sec";
        this.levels = 1;
        this.treeName = "Weaponmaster";
        this.talentType = TalentType.AUGMENT.getType();
        this.tier = 2;
        this.attributeEffects = List.of();
        this.applyType = AttributeApplyType.ON_DMG_APPLY.getType();

        AttributeRequirements attributeRequirements = new AttributeRequirements();
        attributeRequirements.setRequirements(Map.of(ClassTypes.FIGHTER.getType(), 1));
        attributeRequirements.setDependencies(Map.of());

        this.attributeRequirements = attributeRequirements;
    }

    @Override
    public void applyEffect(
            Integer level, TalentService talentService, Stats actorStats, Stats targetStats) {
        double chance = rand.nextDouble(1.0);

        if (chance > 0.2) {
            // 20% chance to activate
            return;
        }
        Instant expire = Instant.now().plusMillis(3000); // lasts for 3 seconds
        String sourceId = actorStats.getActorId();
        Double moveSpeedMultiplier = 0.8; // 20% reduction of speed
        //        Status moveSlow = new MoveMod(expire, sourceId, moveSpeedMultiplier, 1,
        // this.name);
        Status moveSlow =
                new AttributeMod(
                        expire,
                        sourceId,
                        StatsTypes.MOVE_SPEED,
                        0.0,
                        moveSpeedMultiplier,
                        1,
                        this.name);
        ActorStatus actorStatus = new ActorStatus();
        actorStatus.setActorId(targetStats.getActorId());
        actorStatus.setActorStatuses(Set.of(moveSlow));

        talentService.requestAddStatusToActor(actorStatus);
    }
}
