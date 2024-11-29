package server.combat;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.combat.model.ThreatUpdate;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.service.WebsocketClientUpdatesService;

@Slf4j
@KafkaListener(
        groupId = "mmo-server",
        offsetReset = OffsetReset.EARLIEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "status_client")
public class CombatListener {

    @Inject
    WebsocketClientUpdatesService clientUpdatesService;

    @Topic("update-threat-levels")
    public void receiveUpdateActorThreat(ThreatUpdate threatUpdate) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.THREAT_UPDATE.getType())
                        .threatUpdate(threatUpdate)
                        .build();

        clientUpdatesService.sendUpdateToListeningMob(
                socketResponse, threatUpdate.getActorId());
    }

}
