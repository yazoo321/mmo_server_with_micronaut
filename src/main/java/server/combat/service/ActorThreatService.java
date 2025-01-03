package server.combat.service;

import com.mongodb.client.result.DeleteResult;
import io.micronaut.scheduling.annotation.Scheduled;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import server.attribute.status.repository.StatusRepository;
import server.combat.model.ActorThreat;
import server.combat.model.ThreatUpdate;
import server.combat.repository.ActorThreatRepository;
import server.socket.producer.UpdateProducer;

@Singleton
@Slf4j
public class ActorThreatService {

    @Inject ActorThreatRepository actorThreatRepository;

    @Inject UpdateProducer updateProducer;

    @Inject StatusRepository statusRepository;

    private Set<String> trackedActorThreat = new HashSet<>();

    public Single<ActorThreat> addActorThreat(String actorId, String targetId, Integer threat) {
        // TODO: CHANGE ME
        log.info("Adding actor threat: {}", actorId);
        return statusRepository
                .getActorStatuses(actorId)
                .flatMap(
                        status -> {
                            // NOTE: not using status service to avoid circular dependency
                            status.removeOldStatuses();
                            if (status.isDead()) {
                                return null;
                            }

                            trackedActorThreat.add(actorId);
                            return actorThreatRepository
                                    .addThreatToActor(actorId, targetId, threat)
                                    .doOnError(err -> log.error(err.getMessage()))
                                    .doOnSuccess(
                                            updatedThreat -> {
                                                ThreatUpdate update = new ThreatUpdate();
                                                update.setAddThreat(
                                                        Map.of(
                                                                targetId,
                                                                updatedThreat
                                                                        .getActorThreat()
                                                                        .getOrDefault(
                                                                                targetId, threat)));
                                                update.setActorId(actorId);

                                                sendThreatUpdates(update);
                                            });
                        })
                .doOnError(err -> log.error("Failed to add actor threat: {}", err.getMessage()));
    }

    public Single<ActorThreat> removeActorThreat(String actorId, List<String> targetIds) {
        return actorThreatRepository
                .fetchActorThreat(actorId)
                .doOnError(err -> log.error(err.getMessage()))
                .flatMap(
                        threatMap -> {
                            List<String> removed = new ArrayList<>();
                            Map<String, Integer> threatLevels = threatMap.getActorThreat();
                            targetIds.forEach(
                                    id -> {
                                        if (threatLevels.remove(id) != null) {
                                            removed.add(id);
                                        }
                                    });

                            if (threatLevels.isEmpty()) {
                                actorThreatRepository.resetActorThreat(actorId).subscribe();
                                trackedActorThreat.remove(actorId);
                            }

                            if (removed.isEmpty()) {
                                return Single.just(
                                        threatMap); // Return the existing threatMap wrapped in a
                                // Single
                            }

                            ThreatUpdate update = new ThreatUpdate();
                            update.setRemoveThreat(removed);
                            update.setActorId(actorId);

                            sendThreatUpdates(update);

                            return actorThreatRepository.upsertActorThreat(actorId, threatMap);
                        });
    }

    public Single<DeleteResult> resetActorThreat(String actorId) {
        log.info("resetting actor threat: {}", actorId);
        trackedActorThreat.remove(actorId);
        return actorThreatRepository
                .fetchActorThreat(actorId)
                .doOnError(err -> log.error("Failed to reset actor threat, {}", err.getMessage()))
                .onErrorReturnItem(new ActorThreat())
                .flatMap(
                        threatMap -> {
                            Map<String, Integer> threatLevels = threatMap.getActorThreat();

                            List<String> removed = threatLevels.keySet().stream().toList();

                            threatLevels.clear();

                            if (!removed.isEmpty()) {
                                ThreatUpdate update = new ThreatUpdate();
                                update.setRemoveThreat(removed);
                                update.setActorId(actorId);

                                sendThreatUpdates(update);
                            }

                            return actorThreatRepository.resetActorThreat(actorId);
                        });
    }

    private void sendThreatUpdates(ThreatUpdate threatUpdate) {
        log.info("sending threat update! {}", threatUpdate);
        updateProducer.updateThreatLevels(threatUpdate);
    }

    @Scheduled(fixedDelay = "2s")
    void handleThreatDecay() {
        if (trackedActorThreat.isEmpty()) {
            return;
        }
        log.info("Threat decay for: {}", trackedActorThreat);
        actorThreatRepository
                .fetchActorThreat(trackedActorThreat)
                .doOnSuccess(
                        actorThreats -> {
                            actorThreats.forEach(
                                    actorThreat -> {
                                        List<String> threatsToRemove = new ArrayList<>();
                                        Map<String, Integer> threatMap =
                                                actorThreat.getActorThreat();

                                        log.info("threat map: {}", threatMap);

                                        for (Iterator<Map.Entry<String, Integer>> it =
                                                        threatMap.entrySet().iterator();
                                                it.hasNext(); ) {
                                            Map.Entry<String, Integer> entry = it.next();

                                            String k = entry.getKey();
                                            Integer v = entry.getValue();

                                            int updatedThreat = v >> 1;
                                            if (updatedThreat < 6) {
                                                threatsToRemove.add(k);
                                                it.remove();
                                            } else {
                                                entry.setValue(updatedThreat);
                                            }
                                        }

                                        if (!threatsToRemove.isEmpty()) {
                                            removeActorThreat(
                                                    actorThreat.getActorId(), threatsToRemove);
                                        }

                                        ThreatUpdate update = new ThreatUpdate();
                                        update.setAddThreat(threatMap);
                                        update.setActorId(actorThreat.getActorId());
                                        sendThreatUpdates(update);

                                        if (threatMap.isEmpty()) {
                                            trackedActorThreat.remove(actorThreat.getActorId());
                                        }
                                    });
                            // TODO: make this in a batch, this had an issue, perhaps linked with
                            // caching
                            actorThreats.forEach(
                                    at ->
                                            actorThreatRepository
                                                    .updateActorThreat(at.getActorId(), at)
                                                    .doOnError(
                                                            err ->
                                                                    log.error(
                                                                            "error updating threat:"
                                                                                    + " {}",
                                                                            err.getMessage()))
                                                    .subscribe());
                        })
                .doOnError(err -> log.error(err.getMessage()))
                .subscribe();
    }
}
