package server.skills.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.attribute.stats.model.DamageSource;
import server.attribute.status.model.ActorStatus;

@KafkaClient(id = "skill-internal-producer")
public interface SkillProducer {

    @Topic("request-add-actor-status")
    void requestAddStatusToActor(ActorStatus actorStatus);

    @Topic("request-flat-change")
    void requestFlatChange(DamageSource damageSource);

    @Topic("request-take-damage")
    void requestTakeDamage(DamageSource damageSource);

}
