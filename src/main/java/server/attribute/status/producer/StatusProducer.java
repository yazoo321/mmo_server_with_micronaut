package server.attribute.status.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.attribute.stats.model.DamageSource;

@KafkaClient(id = "status-internal-producer")
public interface StatusProducer {

    @Topic("request-take-damage")
    void requestTakeDamage(DamageSource damageSource);

}
