package server.monster.server_integration.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.monster.server_integration.model.MobUpdate;

@KafkaClient(id = "test_client")
public interface TestProducer {

    @Topic("mob-updates")
    void sendFakeUpdate(MobUpdate mobUpdate);
}


//  kafka-topics --create --bootstrap-server localhost:9093 --replication-factor 1
// --partitions 1 --topic test --command-config /etc/kafka/configs/config.properties

// kafka-console-producer --broker-list localhost:9093 --topic test
// --producer.config /etc/kafka/configs/config.properties

// kafka-console-consumer --bootstrap-server localhost:9093 --topic test --from-beginning
// --partition 0
// --consumer.config /etc/kafka/configs/config.properties