package server.monster.server_integration.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.monster.server_integration.model.Monster;

@KafkaClient(id = "mob-server-client")
public interface MonsterServerProducer {

    @Topic("mob-motion-update-result")
    void sendMobUpdateResult(Monster monster);
}
