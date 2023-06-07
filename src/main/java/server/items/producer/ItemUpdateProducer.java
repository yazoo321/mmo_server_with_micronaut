package server.items.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.model.DroppedItem;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;

@KafkaClient(id = "update-producer")
public interface ItemUpdateProducer {

    @Topic("item-added-to-map")
    void pickupItemSuccess(DroppedItem droppedItem);

    @Topic("item-removed-from-map")
    void dropItemSuccess(String itemInstanceId);

}
