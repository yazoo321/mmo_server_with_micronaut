package server.attribute.status.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.attribute.status.model.ActorStatus;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.service.ClientUpdatesService;

@Slf4j
@KafkaListener(
        groupId = "mmo-server",
        offsetReset = OffsetReset.EARLIEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "status_client")
public class StatusListener {

    @Inject ClientUpdatesService clientUpdatesService;

    @Topic("update-actor-status")
    public void receiveUpdateActorStatus(ActorStatus actorStatus) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.STATS_UPDATE.getType())
                        .actorStatus(actorStatus)
                        .build();

        clientUpdatesService.sendUpdateToListeningIncludingSelf(
                socketResponse, actorStatus.getActorId());
    }
}
