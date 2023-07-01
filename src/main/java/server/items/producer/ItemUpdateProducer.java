package server.items.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.items.model.DroppedItem;

@KafkaClient(id = "item-update-producer")
public interface ItemUpdateProducer {

    @Topic("item-added-to-map")
    void pickupItemSuccess(DroppedItem droppedItem);

    @Topic("item-removed-from-map")
    void dropItemSuccess(String itemInstanceId);
}
