package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.DamageSource;
import server.attribute.stats.model.DamageUpdateMessage;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.stats.types.DamageTypes;
import server.attribute.status.model.Status;
import server.attribute.status.producer.StatusProducer;
import server.attribute.status.service.StatusService;
import server.attribute.status.types.StatusTypes;
import server.skills.model.SkillDependencies;

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
    private Consumer<SkillDependencies> applyBurn() {
        log.info("I want to apply burn!");
        return (dependencies) -> {
            try {
                log.info("Applying burn!");
                Map<String, Double> damageMap = new HashMap<>();
                String dmgType = DamageTypes.FIRE.getType();

                damageMap.put(dmgType, this.getDerivedEffects().get(dmgType));
                DamageSource damageSource = new DamageSource();
                damageSource.setDamageMap(damageMap);
                damageSource.setSourceActorId(dependencies.getActorStats().getActorId());
                damageSource.setSourceStatusId(this.getId());

                log.info("requesting producer to take damage! {}", damageMap);

                this.statusProducer.requestTakeDamage(
                        new DamageUpdateMessage(
                                damageSource, dependencies.getTargetStats(), dependencies.getActorStats()));
            } catch (Exception e) {
                log.error("Error applying burn effect, check the value maps");
                throw e;
            }
        };
    }

    @Override
    public Single<Boolean> apply(
            String actorId, StatsService statsService, StatusService statusService, StatusProducer statusProducer) {
        log.info("Burning class applying effect");
        return baseApply(actorId, statsService, statusService, applyBurn(), statusProducer);
    }

}
