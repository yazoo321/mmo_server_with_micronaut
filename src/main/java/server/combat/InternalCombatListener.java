package server.combat;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.DamageSource;
import server.attribute.stats.model.DamageUpdateMessage;
import server.attribute.stats.model.Stats;
import server.combat.service.CombatService;

// TODO: This combat listener should only process messages 'at most once' per message
@Slf4j
@KafkaListener(
        groupId = "internal-combat-listener",
        offsetReset = OffsetReset.EARLIEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "internal-combat-listener")
public class InternalCombatListener {

    @Inject
    CombatService combatService;

    @Topic("processed-damage-updates")
    public void receiveDamageUpdates(DamageUpdateMessage damageUpdateMessage) {
        combatService.handleActorDeath(
                damageUpdateMessage.getTargetStats(), damageUpdateMessage.getOriginStats());
    }

}
