package server.attribute.talents.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import java.util.Map;
import server.attribute.status.model.ActorStatus;
import server.attribute.talents.model.ActorTalents;
import server.attribute.talents.model.Talent;
import server.attribute.talents.producer.TalentProducer;
import server.attribute.talents.repository.TalentRepository;

public class TalentService {

    @Inject TalentProducer talentProducer;

    @Inject TalentRepository talentRepository;

    public void requestAddStatusToActor(ActorStatus actorStatus) {
        talentProducer.requestAddStatusToActor(actorStatus);
    }

    public Single<ActorTalents> getActorTalents(String actorId) {
        return talentRepository.getActorTalents(actorId);
    }

    public Single<Map<Talent, Integer>> getActorTalentsOfApplyType(
            String actorId, String applyType) {
        return talentRepository.getActorTalentsOfApplyType(actorId, applyType);
    }
}
