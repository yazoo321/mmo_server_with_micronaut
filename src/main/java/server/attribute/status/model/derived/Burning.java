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
import server.attribute.common.model.AttributeEffects;
import server.attribute.stats.model.DamageSource;
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

    public Burning(
            Instant expiration,
            String sourceActorId,
            Double damage,
            Integer maxStacks,
            String skillId) {
        log.info("Creating burning status effect, {}, {}, {}", expiration, sourceActorId, damage);
        this.setId(UUID.randomUUID().toString());
        this.setAttributeEffects(new HashMap<>());
        this.getAttributeEffects()
                .put(
                        DamageTypes.FIRE.getType(),
                        new AttributeEffects(DamageTypes.FIRE.getType(), damage, 1.0));
        this.setStatusEffects(new HashSet<>(Set.of(StatusTypes.BURNING.getType())));
        this.setExpiration(expiration);
        this.setMaxStacks(maxStacks);
        this.setOrigin(sourceActorId);
        this.setSkillId(skillId);
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
                AttributeEffects attributeEffects = this.getAttributeEffects().get(dmgType);
                Double dmg =
                        attributeEffects.getAdditiveModifier()
                                * attributeEffects.getMultiplyModifier();
                damageMap.put(dmgType, dmg);
                DamageSource damageSource = new DamageSource();
                damageSource.setDamageMap(damageMap);
                damageSource.setActorId(dependencies.getActorId());
                damageSource.setSourceActorId(this.getOrigin());
                damageSource.setSourceStatusId(this.getId());

                log.info("requesting producer to take damage! {}", damageMap);

                this.statusProducer.requestTakeDamage(damageSource);
            } catch (Exception e) {
                log.error("Error applying burn effect, check the value maps");
                throw e;
            }
        };
    }

    @Override
    public Single<Boolean> applyDamageEffect(
            String actorId, StatusService statusService, StatusProducer statusProducer) {
        log.info("Burning class applying effect");
        return baseApplyDamageEffect(actorId, statusService, applyBurn(), statusProducer);
    }
}
