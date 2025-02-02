package server.attribute.talents.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.attribute.status.model.ActorStatus;

@KafkaClient(id = "talent-internal-producer")
public interface TalentProducer {

    @Topic("request-add-actor-status")
    void requestAddStatusToActor(ActorStatus actorStatus);
}
