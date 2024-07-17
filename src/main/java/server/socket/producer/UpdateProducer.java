package server.socket.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import java.util.List;

import server.attribute.stats.model.DamageSource;
import server.attribute.stats.model.Stats;
import server.attribute.status.model.ActorStatus;
import server.items.equippable.model.EquippedItems;
import server.items.inventory.model.ItemInstanceIds;
import server.items.model.DroppedItem;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;

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

    @Topic("update-actor-stats")
    void updateStats(Stats stats);

    @Topic("damage-updates")
    void updateDamage(DamageSource damageSource);

    @Topic("update-actor-status")
    void updateStatus(ActorStatus actorStatus);
}
