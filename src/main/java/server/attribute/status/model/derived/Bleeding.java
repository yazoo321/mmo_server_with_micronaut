package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.DamageSource;
import server.attribute.stats.types.DamageTypes;
import server.attribute.status.model.Status;
import server.attribute.status.producer.StatusProducer;
import server.attribute.status.service.StatusService;
import server.attribute.status.types.StatusTypes;
import server.skills.model.SkillDependencies;

@Data
@Slf4j
@Serdeable
@NoArgsConstructor
@JsonTypeName("BLEEDING")
@EqualsAndHashCode(callSuper = false)
public class Bleeding extends Status {

    public Bleeding(Instant expiration, String sourceId, Double damage) {
        this.setId(UUID.randomUUID().toString());
        this.setDerivedEffects(new HashMap<>(Map.of(DamageTypes.PHYSICAL.getType(), damage)));
        this.setStatusEffects(new HashSet<>());
        this.setExpiration(expiration);
        this.setCanStack(true);
        this.setOrigin(sourceId);
        this.setCategory(StatusTypes.BLEEDING.getType());
    }

    @Override
    public boolean requiresDamageApply() {
        return true;
    }

    @JsonIgnore
    private Consumer<SkillDependencies> applyBleed() {
        return (dependencies) -> {
            try {
                Map<String, Double> damageMap = new HashMap<>();
                String dmgType = DamageTypes.PHYSICAL.getType();

                damageMap.put(dmgType, this.getDerivedEffects().get(dmgType));
                DamageSource damageSource = new DamageSource();
                damageSource.setDamageMap(damageMap);
                damageSource.setSourceActorId(dependencies.getActorStats().getActorId());
                damageSource.setSourceStatusId(this.getId());

                this.statusProducer.requestTakeDamage(damageSource);

            } catch (Exception e) {
                log.error("Error applying bleed effect, check the value maps");
                throw e;
            }
        };
    }

    @Override
    public Single<Boolean> apply(
            String actorId, StatusService statusService, StatusProducer statusProducer) {
        return baseApply(actorId, statusService, applyBleed(), statusProducer);
    }
}
