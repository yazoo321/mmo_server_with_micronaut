package server.attribute.stats.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.DamageSource;
import server.attribute.stats.model.DamageUpdateMessage;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.service.WebsocketClientUpdatesService;

@Slf4j
@KafkaListener(
        groupId = "stats-listener",
        offsetReset = OffsetReset.LATEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "stats-listener")
public class StatsListener {

    @Inject
    WebsocketClientUpdatesService clientUpdatesService;

    @Inject
    StatsService statsService;

    @Topic("update-actor-stats")
    public void receiveUpdatePlayerAttributes(Stats stats) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.STATS_UPDATE.getType())
                        .stats(stats)
                        .build();

        clientUpdatesService.sendUpdateToListeningIncludingSelf(socketResponse, stats.getActorId());
    }

    @Topic("processed-damage-updates")
    public void receiveDamageUpdates(DamageUpdateMessage damageUpdateMessage) {
        log.info("Received processed-damage-updates message: {}", damageUpdateMessage);
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.DAMAGE_UPDATE.getType())
                        .damageSource(damageUpdateMessage.getDamageSource())
                        .build();

        clientUpdatesService.sendUpdateToListeningIncludingSelf(
                socketResponse, damageUpdateMessage.getOriginStats().getActorId());
    }

    @Topic("request-take-damage")
    public void requestTakeDamage(DamageSource damageSource) {
        log.info("request to take damage received! {}", damageSource);
        statsService.takeDamage(
                damageSource.getActorId(), damageSource.getDamageMap(), damageSource.getSourceActorId());
    }

}
