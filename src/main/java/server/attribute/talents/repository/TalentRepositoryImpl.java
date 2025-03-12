package server.attribute.talents.repository;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import server.attribute.talents.model.ActorTalents;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentTree;
import server.attribute.talents.trees.fighter.weaponmaster.WeaponMasterTree;
import server.attribute.talents.trees.mage.arcanist.ArcanistTree;
import server.common.configuration.MongoConfiguration;

@Slf4j
@Singleton
@CacheConfig("actor-talent-cache")
public class TalentRepositoryImpl {
    // We're not able to easily apply two layer cache into a repository
    // therefore its split into two layers, the main one prefers local cache
    // this one stores in redis cache + using the mongo db

    private static final String TALENT_CACHE = "actor-talent-cache";

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<ActorTalents> actorTalentsCollection;

    @Getter Map<String, Talent> allTalents = new HashMap<>();
    @Getter Map<String, TalentTree> talentTrees = new HashMap<>();

    public TalentRepositoryImpl(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
    }

    @PostConstruct
    private void prepare() {
        prepareTalentCollection();
        prepareTalents();
    }

    private void prepareTalents() {
        TalentTree weaponMasterTree = new WeaponMasterTree();
        TalentTree arcanistTree = new ArcanistTree();
        talentTrees.put(weaponMasterTree.getName(), weaponMasterTree);
        talentTrees.put(arcanistTree.getName(), arcanistTree);
        // put the rest of the available talent trees here
        talentTrees.forEach(
                (k, v) ->
                        v.getTieredTalents()
                                .forEach(
                                        (tier, talents) ->
                                                talents.getTalentList()
                                                        .forEach(
                                                                talent ->
                                                                        allTalents.put(
                                                                                talent.getName(),
                                                                                talent))));
    }

    private void prepareTalentCollection() {
        this.actorTalentsCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getActorTalents(), ActorTalents.class);
    }

    @Cacheable(value = TALENT_CACHE, parameters = "actorId")
    public Single<ActorTalents> getActorTalents(String actorId) {
        return Single.fromPublisher(actorTalentsCollection.find(eq("actorId", actorId)))
                .doOnError(err -> log.error("Failed to fetch actor talents, {}", err.getMessage()));
    }

    @CachePut(value = TALENT_CACHE, parameters = "actorId", async = true)
    public Single<ActorTalents> insertActorTalents(String actorId, ActorTalents actorTalents) {
        return Single.fromPublisher(
                        actorTalentsCollection.replaceOne(eq("actorId", actorId), actorTalents))
                .map(r -> actorTalents);
    }

    @CacheInvalidate(value = TALENT_CACHE, parameters = "actorId")
    public Single<DeleteResult> deleteActorTalents(String actorId) {
        return Single.fromPublisher(actorTalentsCollection.deleteOne(eq("actorId", actorId)));
    }
}
