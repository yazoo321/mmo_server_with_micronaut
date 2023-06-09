package server.items.server_integration.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.items.model.DroppedItem;

@KafkaClient(id = "item-client")
public interface ItemServerProducer {

    @Topic("drop-item-result")
    void sendDropItemResult(DroppedItem droppedItem);
}
