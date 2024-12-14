package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.stats.types.DamageTypes;
import server.attribute.status.model.Status;
import server.attribute.status.service.StatusService;
import server.attribute.status.types.StatusTypes;

@Data
@Serdeable
@NoArgsConstructor
@JsonTypeName("BURNING")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class Burning extends Status {

    public Burning(Instant expiration, String sourceId, Double damage) {
        log.info("Creating burning status effect, {}, {}, {}", expiration, sourceId, damage);
        this.setId(UUID.randomUUID().toString());
        this.setDerivedEffects(new HashMap<>());
        this.getDerivedEffects().put(DamageTypes.FIRE.getType(), damage);
        this.setStatusEffects(new HashSet<>(Set.of(StatusTypes.BURNING.getType())));
        this.setExpiration(expiration);
        this.setCanStack(true);
        this.setOrigin(sourceId);
        this.setCategory(StatusTypes.BURNING.getType());
    }

    @Override
    public boolean requiresDamageApply() {
        return true;
    }

    @JsonIgnore
    private BiConsumer<StatsService, Stats> applyBurn() {
        log.info("I want to apply burn!");
        return (statsService, stats) -> {
            try {
                log.info("Applying burn!!");
                Map<DamageTypes, Double> damageMap = new HashMap<>();
                DamageTypes dmgType = DamageTypes.FIRE;

                damageMap.put(dmgType, this.getDerivedEffects().get(dmgType.getType()));
                log.info("Damage applied: {}", damageMap);
                statsService.takeDamage(stats, damageMap, this.getOrigin());
            } catch (Exception e) {
                log.error("Error applying burn effect, check the value maps");
                throw e;
            }
        };
    }

    @Override
    public Single<Boolean> apply(
            String actorId, StatsService statsService, StatusService statusService) {
        return baseApply(actorId, statsService, statusService, applyBurn());
    }

}
