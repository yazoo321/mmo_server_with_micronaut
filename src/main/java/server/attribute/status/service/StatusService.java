package server.attribute.status.service;

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

    @Inject SessionParamHelper sessionParamHelper;

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
        statusRepository.updateStatus(actorStatus);

        //        updateActorStatusCache(actorStatus);
        ActorStatus update = new ActorStatus(actorStatus.getActorId(), removed, false);
        // notify the user about removed statuses
        updateProducer.updateStatus(update);
    }

    public void addStatusToActor(Set<Status> statuses, String actorId) {
        ActorStatus currentStatuses = statusRepository.getActorStatuses(actorId)
                .doOnError(err-> {log.error(err.getMessage());}).blockingGet();
        currentStatuses.getActorStatuses().addAll(statuses);

        statusRepository.updateStatus(currentStatuses)
                .doOnError(err-> log.error(err.getMessage()))
                .subscribe();

        ActorStatus update = new ActorStatus(actorId, statuses, true);
        updateProducer.updateStatus(update);
    }

    public void initializeStatus(String actorId) {
        statusRepository.updateStatus(new ActorStatus(actorId, Set.of(), false))
                .doOnError(err -> log.error(err.getMessage()))
                .subscribe();
    }

    //    private void updateActorStatusCache(ActorStatus actorStatus) {
    //        CombatData combatData =
    //                sessionParamHelper.getSharedActorCombatData(actorStatus.getActorId());
    //        combatData.setActorStatus(actorStatus);
    //        combatData.setAggregatedStatusDerived(actorStatus.aggregateDerived());
    //        combatData.setAggregatedStatusEffects(actorStatus.aggregateStatusEffects());
    //
    //        sessionParamHelper.setSharedActorCombatData(actorStatus.getActorId(), combatData);
    //    }
}
