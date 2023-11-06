package server.socket.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.attribute.stats.model.Stats;
import server.items.equippable.model.EquippedItems;
import server.items.inventory.model.ItemInstanceIds;
import server.items.model.DroppedItem;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.player.attributes.model.PlayerAttributes;

import java.util.List;

@KafkaClient(id = "general-update-producer")
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

    @Topic("notify-equip-items")
    void notifyEquipItems(List<EquippedItems> equippedItems);

    @Topic("notify-un-equip-items")
    void notifyUnEquipItems(ItemInstanceIds itemInstanceIds);

    @Topic("update-player-attributes")
    void updatePlayerAttributes(PlayerAttributes attributes);

    @Topic("update-actor-stats")
    void updateStats(Stats stats);
}
