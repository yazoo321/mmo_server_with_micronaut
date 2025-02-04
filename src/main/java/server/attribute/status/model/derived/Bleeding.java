package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Single;
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

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Data
@Slf4j
@Serdeable
@NoArgsConstructor
@JsonTypeName("BLEEDING")
@EqualsAndHashCode(callSuper = false)
public class Bleeding extends Status {

    public Bleeding(Instant expiration, String sourceActorId, Double damage, Integer maxStacks, String skillId) {
        this.setId(UUID.randomUUID().toString());
        this.setAttributeEffects(
                Map.of(
                        DamageTypes.PHYSICAL.getType(),
                        new AttributeEffects(DamageTypes.PHYSICAL.getType(), damage, 1.0)));
        this.setStatusEffects(new HashSet<>());
        this.setExpiration(expiration);
        this.setMaxStacks(maxStacks);
        this.setOrigin(sourceActorId);
        this.setSkillId(skillId);
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
                AttributeEffects attributeEffect = this.getAttributeEffects().get(dmgType);
                Double dmg =
                        attributeEffect.getAdditiveModifier()
                                * attributeEffect.getMultiplyModifier();
                damageMap.put(dmgType, dmg);
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
    public Single<Boolean> applyDamageEffect(
            String actorId, StatusService statusService, StatusProducer statusProducer) {
        return baseApplyDamageEffect(actorId, statusService, applyBleed(), statusProducer);
    }
}
