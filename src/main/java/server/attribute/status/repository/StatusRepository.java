package server.attribute.status.repository;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.Cacheable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import server.attribute.status.model.ActorStatus;
import server.common.configuration.MongoConfiguration;

@Slf4j
@Singleton
@CacheConfig("actor-stats-cache,actor-aggregated-statuses,actor-aggregated-derived")
public class StatusRepository {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<ActorStatus> actorStatusCollection;

    private final String ACTOR_STATUS_CACHE = "actor-status-cache";
    private final String ACTOR_AGGREGATED_STATUSES = "actor-aggregated-statuses";
    private final String ACTOR_AGGREGATED_DERIVED = "actor-aggregated-derived";

    public StatusRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        this.actorStatusCollection = getCollection();
    }

    private MongoCollection<ActorStatus> getCollection() {
        return mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getActorStatus(), ActorStatus.class);
    }

    @Cacheable(
            value = ACTOR_STATUS_CACHE,
            parameters = {"actorId"})
    public Single<ActorStatus> getActorStatuses(String actorId) {
        return Single.fromPublisher(actorStatusCollection.find(eq("actorId", actorId)));
    }

    @CacheInvalidate(
            value = ACTOR_STATUS_CACHE,
            parameters = {"actorId"})
    @CacheInvalidate(
            value = ACTOR_AGGREGATED_STATUSES,
            parameters = {"actorId"})
    @CacheInvalidate(
            value = ACTOR_AGGREGATED_DERIVED,
            parameters = {"actorId"})
//    TODO: merge parameter for actorStatus for cache
    public Single<ActorStatus> updateStatus(String actorId, ActorStatus actorStatus) {
        Bson filter = Filters.eq("actorId", actorStatus.getActorId());
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        return Single.fromPublisher(actorStatusCollection.replaceOne(filter, actorStatus, options))
                .map(res -> actorStatus);
    }

    @Cacheable(
            value = ACTOR_AGGREGATED_STATUSES,
            parameters = {"actorId"})
    public Single<Set<String>> getAggregatedStatuses(String actorId) {
        return getActorStatuses(actorId).map(ActorStatus::aggregateStatusEffects);
    }

    @Cacheable(
            value = ACTOR_AGGREGATED_DERIVED,
            parameters = {"actorId"})
    public Single<Map<String, Double>> getAggregatedDerived(String actorId) {
        return getActorStatuses(actorId).map(ActorStatus::aggregateDerived);
    }

    @CacheInvalidate(
            value = ACTOR_STATUS_CACHE,
            parameters = {"actorId"})
    @CacheInvalidate(
            value = ACTOR_AGGREGATED_STATUSES,
            parameters = {"actorId"})
    @CacheInvalidate(
            value = ACTOR_AGGREGATED_DERIVED,
            parameters = {"actorId"})
    public Single<DeleteResult> deleteActorStatuses(String actorId) {
        return Single.fromPublisher(actorStatusCollection.deleteOne(eq("actorId", actorId)));
    }
}
