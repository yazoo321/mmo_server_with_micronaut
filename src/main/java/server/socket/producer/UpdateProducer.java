package server.socket.producer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import server.attribute.stats.model.DamageUpdateMessage;
import server.attribute.stats.model.Stats;
import server.attribute.status.model.ActorStatus;
import server.combat.model.ThreatUpdate;
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

    @Topic("remove-mobs-from-game")
    void removeMobsFromGame(String actorIds);

    @Topic("player-motion-message")
    void sendPlayerMotionUpdate(PlayerMotion playerMotion);

    @Topic("item-added-to-map")
    void addItemToMap(DroppedItem droppedItem);

    @Topic("item-removed-from-map")
    void removeItemFromMap(String itemInstanceId);

    @Topic("notify-equip-items")
    void notifyEquipItems(EquippedItems equippedItems);

    @Topic("notify-un-equip-items")
    void notifyUnEquipItems(ItemInstanceIds itemInstanceIds);

    @Topic("update-actor-stats")
    void updateStats(Stats stats);

    @Topic("processed-damage-updates")
    void updateDamage(DamageUpdateMessage damageUpdateMessage);

    @Topic("notify-actor-death")
    void notifyActorDeath(DamageUpdateMessage damageUpdateMessage);

    @Topic("update-actor-status")
    void updateStatus(ActorStatus actorStatus);

    // this is used to update the stats
    @Topic("update-actor-status-internal")
    void updateStatusInternal(ActorStatus actorStatus);

    @Topic("update-threat-levels")
    void updateThreatLevels(ThreatUpdate threatUpdate);

    @Topic("update-threat-levels")
    void updateThreatLevels(String threatUpdate);
}
