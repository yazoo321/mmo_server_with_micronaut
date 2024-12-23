package server.attribute.status.service;

import com.mongodb.client.result.DeleteResult;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.repository.StatusRepository;
import server.socket.producer.UpdateProducer;

@Singleton
@Slf4j
public class StatusService {

    @Inject StatusRepository statusRepository;

    @Inject UpdateProducer updateProducer;

    public Single<ActorStatus> getActorStatus(String actorId) {
//        log.info("fetching actor status: {}", actorId);
        return statusRepository.getActorStatuses(actorId)
                .map(this::removeExpiredStatuses);
    }

    public ActorStatus removeExpiredStatuses(ActorStatus actorStatus) {
        Set<Status> removed = actorStatus.removeOldStatuses();
        if (removed.isEmpty()) {
            return actorStatus;
        }

        actorStatus.getActorStatuses().removeAll(removed);
        statusRepository.updateStatus(actorStatus.getActorId(), actorStatus).subscribe();

        ActorStatus update = new ActorStatus(actorStatus.getActorId(), removed, false, actorStatus.aggregateStatusEffects());
        // notify the user about removed statuses
        updateProducer.updateStatus(update);

        return actorStatus;
    }

    public void removeStatusFromActor(ActorStatus actorStatus, Set<Status> statuses) {
        Set<String> statusIds = statuses.stream().map(Status::getId).collect(Collectors.toSet());
        actorStatus.setActorStatuses(
                actorStatus.getActorStatuses().stream().filter(s -> statusIds.contains(s.getId()))
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

    public void addStatusToActor(ActorStatus actorStatus, Set<Status> statuses) {
        actorStatus.getActorStatuses().addAll(statuses);
        actorStatus.aggregateStatusEffects();

        statusRepository
                .updateStatus(actorStatus.getActorId(), actorStatus)
                .doOnError(err -> log.error(err.getMessage()))
                .subscribe();

        ActorStatus update = new ActorStatus(actorStatus.getActorId(), statuses, true, null);
        update.setStatusEffects(actorStatus.getStatusEffects());
        updateProducer.updateStatus(update);
    }

    public Single<ActorStatus> removeAllStatuses(String actorId) {
        return statusRepository
                .getActorStatuses(actorId)
                .flatMap(status -> {
                    ActorStatus update = new ActorStatus(actorId, status.getActorStatuses(), false, Set.of());

                    updateProducer.updateStatus(update);

                    status.getActorStatuses().clear();
                    status.aggregateStatusEffects();

                    return statusRepository
                            .updateStatus(actorId, status)
                            .doOnError(err -> log.error(err.getMessage()));
                })
                .doOnError(er -> log.error("Failed to handle respawn statuses: {}", er.getMessage()));

    }

    public Single<DeleteResult> deleteActorStatus(String actorId) {
        return statusRepository.deleteActorStatuses(actorId);
    }

    public void initializeStatus(String actorId) {
        statusRepository
                .updateStatus(actorId, new ActorStatus(actorId, Set.of(), false, Set.of()))
                .doOnError(err -> log.error(err.getMessage()))
                .subscribe();
    }

}
