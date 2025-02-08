package server.combat.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.DamageUpdateMessage;
import server.combat.model.ThreatUpdate;
import server.combat.service.CombatService;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.service.WebsocketClientUpdatesService;

// TODO: This combat listener should only process messages 'at most once' per message
@Slf4j
@KafkaListener(
        groupId = "internal-combat-listener-group",
        offsetReset = OffsetReset.LATEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "internal-combat-listener-client")
public class SingleInternalCombatListener {

    @Inject CombatService combatService;

    @Inject WebsocketClientUpdatesService clientUpdatesService;


    @Topic("notify-actor-death")
    void receive_actor_death_notify(DamageUpdateMessage damageUpdateMessage) {
        combatService.handleActorDeath(damageUpdateMessage);
    }

    @Topic("update-threat-levels")
    public void receiveUpdateActorThreat(ThreatUpdate threatUpdate) {
        log.info("Received actor threat update in kafka, passing to clients");
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.THREAT_UPDATE.getType())
                        .threatUpdate(threatUpdate)
                        .build();

        clientUpdatesService.sendUpdateToListeningMob(socketResponse, threatUpdate.getActorId());
    }
}
