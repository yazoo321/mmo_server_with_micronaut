package server.attribute.status.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.attribute.stats.model.DamageUpdateMessage;

@KafkaClient(id = "status-internal-producer")
public interface StatusProducer {

    @Topic("request-take-damage")
    void requestTakeDamage(DamageUpdateMessage damageUpdateMessage);

}
