package server.attribute.status.service;

import com.mongodb.client.result.DeleteResult;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.repository.StatusRepository;
import server.session.SessionParamHelper;
import server.socket.producer.UpdateProducer;

@Singleton
@Slf4j
public class StatusService {

    @Inject StatusRepository statusRepository;

    @Inject UpdateProducer updateProducer;

    public ActorStatus getActorStatus(String actorId) {
        return statusRepository.getActorStatuses(actorId).blockingGet();
    }

    public void removeExpiredStatuses(ActorStatus actorStatus) {
        Set<Status> removed = actorStatus.removeOldStatuses();
        if (removed.isEmpty()) {
            return;
        }

        actorStatus.getActorStatuses().removeAll(removed);
        statusRepository.updateStatus(actorStatus.getActorId(), actorStatus);

        ActorStatus update = new ActorStatus(actorStatus.getActorId(), removed, false);
        // notify the user about removed statuses
        updateProducer.updateStatus(update);
    }

    public void addStatusToActor(Set<Status> statuses, String actorId) {
        ActorStatus currentStatuses =
                statusRepository
                        .getActorStatuses(actorId)
                        .doOnError(
                                err -> {
                                    log.error(err.getMessage());
                                })
                        .blockingGet();
        currentStatuses.getActorStatuses().addAll(statuses);

        statusRepository
                .updateStatus(currentStatuses.getActorId(), currentStatuses)
                .doOnError(err -> log.error(err.getMessage()))
                .subscribe();

        ActorStatus update = new ActorStatus(actorId, statuses, true);
        updateProducer.updateStatus(update);
    }

    public Single<DeleteResult> deleteActorStatus(String actorId) {
        return statusRepository.deleteActorStatuses(actorId);
    }

    public void initializeStatus(String actorId) {
        statusRepository
                .updateStatus(actorId, new ActorStatus(actorId, Set.of(), false))
                .doOnError(err -> log.error(err.getMessage()))
                .subscribe();
    }

}
