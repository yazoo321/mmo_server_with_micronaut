package server.attribute.status.service;

import com.mongodb.client.result.DeleteResult;
import io.micronaut.scheduling.annotation.Scheduled;
import io.netty.util.internal.ConcurrentSet;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Dead;
import server.attribute.status.producer.StatusProducer;
import server.attribute.status.repository.StatusRepository;
import server.session.SessionParamHelper;
import server.socket.producer.UpdateProducer;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class StatusService {

    @Inject StatusRepository statusRepository;

    @Inject UpdateProducer updateProducer;

    @Inject SessionParamHelper sessionParamHelper;

    @Inject StatusProducer statusProducer;

    ConcurrentSet<String> syncActorStatuses = new ConcurrentSet<>();

    public Single<ActorStatus> getActorStatus(String actorId) {
        //        log.info("fetching actor status: {}", actorId);
        return statusRepository
                .getActorStatuses(actorId)
                .doOnError(
                        err ->
                                log.error(
                                        "Failed to get actor statuses for ID: {}, {}",
                                        actorId,
                                        err.getMessage()));
    }

    public ActorStatus removeExpiredStatuses(ActorStatus actorStatus) {
        Set<Status> removed = actorStatus.removeOldStatuses();
        if (removed.isEmpty()) {
            return actorStatus;
        }

        log.info("current timestamp: {}", Instant.now());
        log.info("Removing old statuses: {}", removed);
        actorStatus.getActorStatuses().removeAll(removed);
        statusRepository.updateStatus(actorStatus.getActorId(), actorStatus).subscribe();

        ActorStatus update =
                new ActorStatus(
                        actorStatus.getActorId(),
                        removed,
                        false,
                        actorStatus.aggregateStatusEffects());
        // notify the user about removed statuses
        updateProducer.updateStatus(update);

        return actorStatus;
    }

    public void removeStatusFromActor(ActorStatus actorStatus, Set<Status> statuses) {
        Set<String> statusIds = statuses.stream().map(Status::getId).collect(Collectors.toSet());
        actorStatus.setActorStatuses(
                actorStatus.getActorStatuses().stream()
                        .filter(s -> statusIds.contains(s.getId()))
                        .collect(Collectors.toSet()));
        actorStatus.aggregateStatusEffects();

        statusRepository
                .updateStatus(actorStatus.getActorId(), actorStatus)
                .doOnError(err -> log.error(err.getMessage()))
                .subscribe();

        ActorStatus update = new ActorStatus(actorStatus.getActorId(), statuses, false, null);
        update.setStatusEffects(actorStatus.getStatusEffects());
        updateProducer.updateStatus(update);
    }

    public void addStatusToActor(String actorId, Set<Status> statuses) {
        getActorStatus(actorId)
                .doOnSuccess(actorStatus -> addStatusToActor(actorStatus, statuses))
                .subscribe();
    }

    public void addStatusToActor(ActorStatus actorStatus, Set<Status> statuses) {
        actorStatus.aggregateStatusEffects();
        if (actorStatus.getStatusEffects().contains("DEAD")) {
            log.info("Skipping adding statuses: {} as the actor is dead", statuses);
            return;
        }

        log.info("Adding statuses to actor: {}", statuses);
        actorStatus.getActorStatuses().addAll(statuses);
        actorStatus.aggregateStatusEffects();

        statusRepository
                .updateStatus(actorStatus.getActorId(), actorStatus)
                .doOnError(err -> log.error(err.getMessage()))
                .blockingSubscribe();

        ActorStatus update = new ActorStatus(actorStatus.getActorId(), statuses, true, null);
        update.setStatusEffects(actorStatus.getStatusEffects());
        updateProducer.updateStatus(update);
    }

    public Single<ActorStatus> removeAllStatuses(String actorId) {
        return statusRepository
                .getActorStatuses(actorId)
                .flatMap(
                        status -> {
                            ActorStatus update =
                                    new ActorStatus(
                                            actorId, status.getActorStatuses(), false, Set.of());

                            updateProducer.updateStatus(update);

                            status.getActorStatuses().clear();
                            status.aggregateStatusEffects();

                            return statusRepository
                                    .updateStatus(actorId, status)
                                    .doOnError(err -> log.error(err.getMessage()));
                        })
                .doOnError(
                        er -> log.error("Failed to handle respawn statuses: {}", er.getMessage()));
    }

    public Single<DeleteResult> deleteActorStatus(String actorId) {
        return statusRepository.deleteActorStatuses(actorId);
    }

    public Single<ActorStatus> initializeStatus(String actorId) {
        log.info("Initialising status for actor: {}", actorId);
        return statusRepository
                .updateStatus(actorId, new ActorStatus(actorId, Set.of(), false, Set.of()))
                .doOnError(err -> log.error(err.getMessage()));
    }

    public void handleActorDeath(Stats actorStats) {
        removeAllStatuses(actorStats.getActorId())
            .doOnSuccess(statuses -> addStatusToActor(statuses, Set.of(new Dead())))
            .subscribe();

        if (!actorStats.isPlayer()) {
            deleteActorStatus(actorStats.getActorId())
                    .doOnError(err -> log.error(err.getMessage()))
                    .delaySubscription(10_000, TimeUnit.MILLISECONDS)
                    .subscribe();
        }

    }

    @Scheduled(fixedDelay = "300ms")
    public void processStatusesForActivePlayers() {
        if (sessionParamHelper.getLiveSessions() == null) {
            // this can/should only occur in tests with incomplete mocks
            return;
        }

        sessionParamHelper
                .getLiveSessions()
                .forEach(
                        (k, v) -> {
                            if (SessionParamHelper.getIsServer(v)) {
                                syncActorStatuses.addAll(SessionParamHelper.getTrackingMobs(v));
                            } else {
                                syncActorStatuses.add(k);
                            }
                        });

        // due to cache system, more difficult to parallelize
        // TODO: Try to parallelize this?
        syncActorStatuses.parallelStream()
                .forEach(
                        actor -> {
                            syncActorStatuses.remove(actor);
                            getActorStatus(actor)
                                    .map(this::removeExpiredStatuses)
                                    .doOnError(
                                            err ->
                                                    log.error(
                                                            "Error applying status effects, err:"
                                                                    + " {}",
                                                            err.getMessage()))
                                    .onErrorComplete()
                                    .doOnSuccess(
                                            actorStatus -> {
                                                if (actorStatus.getActorStatuses() == null
                                                        || actorStatus
                                                                .getActorStatuses()
                                                                .isEmpty()) {
                                                    return;
                                                }
                                                // TODO: return single .map of this with one
                                                // subscribe?
                                                actorStatus.getActorStatuses().parallelStream()
                                                        .forEach(
                                                                s -> {
                                                                    if (s.requiresDamageApply()) {
                                                                        s.applyDamageEffect(
                                                                                        actor,
                                                                                        this,
                                                                                        statusProducer)
                                                                                .doOnError(
                                                                                        err ->
                                                                                                log
                                                                                                        .error(
                                                                                                                "error"
                                                                                                                    + " in scheduled"
                                                                                                                    + " status"
                                                                                                                    + " applier"
                                                                                                                    + " for status:"
                                                                                                                    + " {}, error:"
                                                                                                                    + " {}",
                                                                                                                s,
                                                                                                                err
                                                                                                                        .getMessage()))
                                                                                .subscribe();
                                                                    }
                                                                });
                                            })
                                    .subscribe();
                        });
    }
}
