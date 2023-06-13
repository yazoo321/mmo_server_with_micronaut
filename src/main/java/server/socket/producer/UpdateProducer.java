package server.socket.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.items.model.DroppedItem;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;

@KafkaClient(id = "update-producer")
public interface UpdateProducer {

    @Topic("mob-motion-update")
    void sendMobMotionUpdate(Monster monster);

    @Topic("create-mob")
    void sendCreateMob(Monster monster);

    @Topic("player-motion-update")
    void sendPlayerMotionUpdate(PlayerMotion playerMotion);

    @Topic("item-added-to-map")
    void addItemToMap(DroppedItem droppedItem);

    @Topic("item-removed-from-map")
    void removeItemFromMap(String itemInstanceId);
}
