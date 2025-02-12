package server.attribute.stats.listener;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeEffects;
import server.attribute.stats.model.DamageSource;
import server.attribute.stats.service.StatsService;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;

@Slf4j
@KafkaListener(
        groupId = "single-stats-listener",
        offsetReset = OffsetReset.LATEST,
        offsetStrategy = OffsetStrategy.SYNC,
        clientId = "single-stats-listener")
public class SingleStatsListener {

    @Inject StatsService statsService;

    @Topic("request-take-damage")
    public void requestTakeDamage(DamageSource damageSource) {
        log.info("request to take damage received! {}", damageSource);
        statsService.takeDamage(
                damageSource.getActorId(),
                damageSource.getDamageMap(),
                damageSource.getSourceActorId());
    }

    @Topic("request-flat-change")
    public void requestFlatChange(DamageSource damageSource) {
        log.info("request for flat damage (positive or negative) received! {}", damageSource);
        // hp should always be positive, mp can be negative
        statsService.flatHP_MP_Mod(damageSource);
    }

    @Topic("update-actor-status")
    public void receive_actor_statuses(ActorStatus actorStatus) {
        log.info("stats service received status update {}", actorStatus);

        Map<String, AttributeEffects> statusesAffectingStats = new HashMap<>();

        for (Status status : actorStatus.getActorStatuses()) {
            if (status.requiresStatsUpdate()) {
                merge(statusesAffectingStats, status.getAttributeEffects());
            }
        }

        // TODO: Consider optimisation, do we _need_ to update this? this is triggered when stats
        // update is not required.
        // for example, we can create a separate event for more precise updates, or fetch status now
        // and compare if diff

        statsService
                .getStatsFor(actorStatus.getActorId())
                .doOnSuccess(
                        actorStats -> {
                            actorStats.setStatusEffects(statusesAffectingStats);
                            statsService.evaluateDerivedStats(actorStats);
                        })
                .subscribe();
    }

    private void merge(Map<String, AttributeEffects> left, Map<String, AttributeEffects> right) {
        right.forEach(
                (k, v) -> {
                    if (left.containsKey(k)) {
                        AttributeEffects eff = left.get(k);
                        eff.setAdditiveModifier(
                                eff.getAdditiveModifier() + v.getAdditiveModifier());
                        eff.setMultiplyModifier(
                                eff.getMultiplyModifier() * v.getMultiplyModifier());
                    } else {
                        left.put(k, v);
                    }
                });
    }
}
