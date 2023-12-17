package server.attribute.status.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import server.attribute.stats.repository.ActorStatsRepository;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.repository.StatusRepository;
import server.combat.model.CombatData;
import server.session.SessionParamHelper;
import server.socket.producer.UpdateProducer;

import java.util.List;

@Singleton
public class StatusService {

    @Inject
    StatusRepository statusRepository;

    @Inject
    SessionParamHelper sessionParamHelper;

    @Inject
    UpdateProducer updateProducer;

    public ActorStatus getActorStatus(String actorId) {
        ActorStatus actorStatus = statusRepository.getActorStatuses(actorId).blockingGet();
        updateActorStatusCache(actorStatus);

        return actorStatus;
    }

    public void removeExpiredStatuses(ActorStatus actorStatus) {
        List<Status> removed = actorStatus.removeOldStatuses();
        if (removed.isEmpty()) {
            return;
        }

        updateActorStatusCache(actorStatus);
        ActorStatus update = new ActorStatus(actorStatus.getActorId(), removed, false);
        // notify the user about removed statuses
        updateProducer.updateStatus(update);
    }

    public void addStatusToActor(List<Status> statuses, String actorId) {
        CombatData combatData = sessionParamHelper.getSharedActorCombatData(actorId);
        ActorStatus actorStatus = combatData.getActorStatus();
        actorStatus.getActorStatuses().addAll(statuses);
        updateActorStatusCache(actorStatus);

        statusRepository.updateStatus(actorStatus).subscribe();

        ActorStatus update = new ActorStatus(actorId, statuses, true);
        updateProducer.updateStatus(update);
    }

    private void updateActorStatusCache(ActorStatus actorStatus) {
        CombatData combatData = sessionParamHelper.getSharedActorCombatData(actorStatus.getActorId());
        combatData.setActorStatus(actorStatus);
        combatData.setAggregatedStatusDerived(actorStatus.aggregateDerived());
        combatData.setAggregatedStatusEffects(actorStatus.aggregateStatusEffects());

        sessionParamHelper.setSharedActorCombatData(actorStatus.getActorId(), combatData);
    }

}
