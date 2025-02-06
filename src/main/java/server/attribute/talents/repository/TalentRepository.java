package server.attribute.talents.repository;

import com.mongodb.client.result.DeleteResult;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import server.attribute.talents.model.ActorTalents;
import server.attribute.talents.model.Talent;

@Slf4j
@Singleton
@CacheConfig("talentLocalCache")
public class TalentRepository {

    private static final String TALENT_LOCAL_CACHE = "talentLocalCache";

    Map<String, Talent> allTalents = new HashMap<>();

    // Double layer cache
    // Talent repository is mainly handling the caching of data locally
    // TalentRepositoryImpl is package private, holding core implementation
    @Inject TalentRepositoryImpl talentRepository;

    @Cacheable(value = TALENT_LOCAL_CACHE, parameters = "actorId")
    public Single<ActorTalents> getActorTalents(String actorId) {
        return talentRepository.getActorTalents(actorId);
    }

    @CachePut(value = TALENT_LOCAL_CACHE, parameters = "actorId", async = true)
    public Single<ActorTalents> insertActorTalents(String actorId, ActorTalents actorTalents) {
        return talentRepository.insertActorTalents(actorId, actorTalents);
    }

    public Talent getTalentByName(String name) {
        return talentRepository.getAllTalents().get(name);
    }

    @Cacheable(
            value = TALENT_LOCAL_CACHE,
            parameters = {"actorId", "applyType"})
    public Single<Map<Talent, Integer>> getActorTalentsOfApplyType(
            String actorId, String applyType) {
        // this will be called quite often as part of combat service, hence looking to optimise
        return getActorTalents(actorId)
                .map(
                        actorTalents -> {
                            Map<Talent, Integer> map = new HashMap<>();
                            actorTalents
                                    .getLearnedTalents()
                                    .forEach(
                                            (k, v) -> {
                                                Talent talent =
                                                        talentRepository.getAllTalents().get(k);
                                                if (talent.getApplyType().equals(applyType)) {
                                                    map.put(talent, v);
                                                }
                                            });
                            return map;
                        });
    }

    @CacheInvalidate(value = TALENT_LOCAL_CACHE, parameters = "actorId")
    @CacheInvalidate(
            value = TALENT_LOCAL_CACHE,
            parameters = {"actorId", "applyType"})
    public Single<DeleteResult> deleteActorTalents(String actorId) {
        return talentRepository.deleteActorTalents(actorId);
    }
}
