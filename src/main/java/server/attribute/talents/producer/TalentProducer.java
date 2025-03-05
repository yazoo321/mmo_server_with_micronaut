package server.attribute.talents.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.attribute.stats.model.DamageSource;
import server.attribute.status.model.ActorStatus;
import server.attribute.talents.model.ActorTalentAttributeEffects;

@KafkaClient(id = "talent-internal-producer")
public interface TalentProducer {

    @Topic("request-add-actor-status")
    void requestAddStatusToActor(ActorStatus actorStatus);

    @Topic("request-flat-change")
    void requestFlatChange(DamageSource damageSource);

    @Topic("request-talent-learn")
    void requestTalentLearn(ActorTalentAttributeEffects talentAttributeEffects);
}
