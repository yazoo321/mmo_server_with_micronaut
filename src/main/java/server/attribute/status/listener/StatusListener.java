package server.attribute.status.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.DamageUpdateMessage;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.service.StatusService;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.service.WebsocketClientUpdatesService;

@Slf4j
@KafkaListener(
        groupId = "status-listener",
        offsetReset = OffsetReset.LATEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "status-listener")
public class StatusListener {

    @Inject WebsocketClientUpdatesService clientUpdatesService;

    @Inject StatusService statusService;

    @Topic("update-actor-status")
    public void receiveUpdateActorStatus(ActorStatus actorStatus) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.STATUS_UPDATE.getType())
                        .actorStatus(actorStatus)
                        .build();

        clientUpdatesService.sendUpdateToListeningIncludingSelf(
                socketResponse, actorStatus.getActorId());
    }

    @Topic("request-add-actor-status")
    public void requestAddActorStatus(ActorStatus actorStatus) {
        // should only populate: String actorId; Set<Status> actorStatuses;
        statusService.addStatusToActor(actorStatus.getActorId(), actorStatus.getActorStatuses());
    }

    @Topic("notify-actor-death")
    void receive_actor_death_notify(DamageUpdateMessage damageUpdateMessage) {

    }
}
