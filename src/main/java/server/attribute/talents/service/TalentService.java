package server.attribute.talents.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import server.attribute.stats.model.DamageSource;
import server.attribute.status.model.ActorStatus;
import server.attribute.talents.model.ActorTalents;
import server.attribute.talents.model.Talent;
import server.attribute.talents.producer.TalentProducer;
import server.attribute.talents.repository.TalentRepository;

import java.util.HashMap;
import java.util.Map;

public class TalentService {

    @Inject TalentProducer talentProducer;

    @Inject TalentRepository talentRepository;

    public void requestAddStatusToActor(ActorStatus actorStatus) {
        talentProducer.requestAddStatusToActor(actorStatus);
    }

    public void requestStatChange(DamageSource damageSource) {
        talentProducer.requestFlatChange(damageSource);
    }

    public Single<ActorTalents> getActorTalents(String actorId) {
        return talentRepository.getActorTalents(actorId);
    }

    public Single<Map<Talent, Integer>> getActorTalentsOfApplyType(
            String actorId, String applyType) {
        return talentRepository.getActorTalentsOfApplyType(actorId, applyType);
    }

    public Single<ActorTalents> initializeActorTalents(String actorId) {
        ActorTalents talents = new ActorTalents();
        talents.setLearnedTalents(new HashMap<>());
        talents.setActorId(actorId);

        return talentRepository.insertActorTalents(actorId, talents);
    }
}
