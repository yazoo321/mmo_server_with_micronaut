package server.faction.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import server.faction.model.ActorAllegiance;
import server.faction.repository.ActorAllegianceRepository;

@Singleton
public class ActorAllegianceService {

    @Inject private ActorAllegianceRepository actorAllegianceRepository;

    Single<List<ActorAllegiance>> getActorAllegiance(String actorId) {
        return actorAllegianceRepository.findByActorId(actorId);
    }
}
