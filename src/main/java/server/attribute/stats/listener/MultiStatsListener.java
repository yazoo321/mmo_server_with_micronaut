package server.attribute.stats.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.DamageUpdateMessage;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.PlayerLevelStatsService;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.socket.service.WebsocketClientUpdatesService;

@Slf4j
@KafkaListener(
        groupId = "stats-listener",
        uniqueGroupId = true,
        offsetReset = OffsetReset.LATEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "stats-listener")
public class MultiStatsListener {

    @Inject WebsocketClientUpdatesService clientUpdatesService;

    @Inject PlayerLevelStatsService playerLevelStatsService;

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
        // relay this to the clients
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.DAMAGE_UPDATE.getType())
                        .damageSource(damageUpdateMessage.getDamageSource())
                        .build();

        clientUpdatesService.sendUpdateToListeningIncludingSelf(
                socketResponse, damageUpdateMessage.getOriginStats().getActorId());
    }

    @Topic("notify-actor-death")
    void receive_actor_death_notify(DamageUpdateMessage damageUpdateMessage) {
        if (damageUpdateMessage.getOriginStats().isPlayer()) {
            playerLevelStatsService.handleAddXp(
                    damageUpdateMessage.getTargetStats(), damageUpdateMessage.getOriginStats());
        }
    }

}
