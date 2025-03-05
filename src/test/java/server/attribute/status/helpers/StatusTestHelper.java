package server.attribute.status.helpers;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.repository.StatusRepository;

@Singleton
public class StatusTestHelper {

    @Inject StatusRepository statusRepository;

    public void resetStatuses(List<String> actors) {
        actors.forEach(actor -> statusRepository.deleteActorStatuses(actor).blockingSubscribe());

        actors.forEach(
                actor -> {
                    ActorStatus defaultActorStatus = new ActorStatus();
                    defaultActorStatus.setActorId(actor);
                    defaultActorStatus.setActorStatuses(new HashSet<>());
                    defaultActorStatus.setStatusEffects(new HashSet<>());

                    statusRepository
                            .createActorStatus(actor, defaultActorStatus)
                            .blockingSubscribe();
                });
    }
}
