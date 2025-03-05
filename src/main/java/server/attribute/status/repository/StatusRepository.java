package server.attribute.status.repository;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.Cacheable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.common.configuration.MongoConfiguration;
import server.socket.producer.UpdateProducer;

@Slf4j
@Singleton
@CacheConfig("actor-status-cache,actor-aggregated-statuses,actor-aggregated-derived")
public class StatusRepository {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<ActorStatus> actorStatusCollection;

    private final String ACTOR_STATUS_CACHE = "actor-status-cache";
    private final String ACTOR_AGGREGATED_STATUSES = "actor-aggregated-statuses";
    private final String ACTOR_AGGREGATED_DERIVED = "actor-aggregated-derived";

    @Inject
    UpdateProducer updateProducer;

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
        return Single.fromPublisher(actorStatusCollection.find(eq("actorId", actorId)))
                .doOnError(
                        err -> log.error("Failed to fetch actor statuses, {}", err.getMessage()));
    }


    @CacheInvalidate(
            value = {ACTOR_STATUS_CACHE, ACTOR_AGGREGATED_STATUSES},
            parameters = {"actorId"},
            async = true)
    public Single<ActorStatus> addStatuses(String actorId, Set<Status> newStatuses) {
        log.info("Updating actor statuses for: {}, adding statuses: {}", actorId, newStatuses);
        Bson filter = Filters.eq("actorId", actorId);
        Bson update = Updates.addEachToSet("actorStatuses", Arrays.asList(newStatuses.toArray()));

        return Single.fromPublisher(actorStatusCollection.findOneAndUpdate(filter, update,
                        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)))
                .doOnSuccess(updatedActorStatus -> {
                    updateProducer.updateStatusInternal(updatedActorStatus);

                    ActorStatus clientUpdate = new ActorStatus(actorId, newStatuses, true, null);
                    updateProducer.updateStatus(clientUpdate);
                });
    }

    @CacheInvalidate(
            value = {ACTOR_STATUS_CACHE, ACTOR_AGGREGATED_STATUSES},
            parameters = {"actorId"},
            async = true)
    public Single<ActorStatus> removeStatuses(String actorId, Set<Status> statusesToRemove) {
        log.info("Updating actor statuses for: {}, removing statuses: {}", actorId, statusesToRemove);
        Bson filter = Filters.eq("actorId", actorId);
        Bson update = Updates.pullAll("actorStatuses", Arrays.asList(statusesToRemove.toArray()));

        return Single.fromPublisher(actorStatusCollection.findOneAndUpdate(filter, update,
                        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)))
                .doOnSuccess(updatedActorStatus -> {
                    updateProducer.updateStatusInternal(updatedActorStatus);

                    ActorStatus clientUpdate = new ActorStatus(actorId, statusesToRemove, false, null);
                    updateProducer.updateStatus(clientUpdate);
                });
    }


    @CacheInvalidate(
            value = {ACTOR_STATUS_CACHE, ACTOR_STATUS_CACHE, ACTOR_AGGREGATED_STATUSES},
            parameters = {"actorId"},
            async = true)
    //    TODO: merge parameter for actorStatus for cache
    public Single<ActorStatus> createActorStatus(String actorId, ActorStatus actorStatus) {
        log.info("Updating actor statuses for: {}, {}", actorId, actorStatus);
        Bson filter = Filters.eq("actorId", actorStatus.getActorId());
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        return Single.fromPublisher(actorStatusCollection.replaceOne(filter, actorStatus, options))
                .map(res -> {
                    if (res.wasAcknowledged()) {
                        // notify internal status update, which will update stats
                        updateProducer.updateStatusInternal(actorStatus);
                    }
                    return actorStatus;
                });
    }

//    @Cacheable(
//            value = ACTOR_AGGREGATED_STATUSES,
//            parameters = {"actorId"})
//    public Single<Set<String>> getAggregatedStatuses(String actorId) {
//        return getActorStatuses(actorId).map(ActorStatus::aggregateStatusEffects);
//    }

    @CacheInvalidate(
            value = {ACTOR_STATUS_CACHE, ACTOR_STATUS_CACHE, ACTOR_AGGREGATED_STATUSES},
            parameters = {"actorId"},
            async = true)
    public Single<DeleteResult> deleteActorStatuses(String actorId) {
        // TODO: should be deleteOne, but sometimes tests flake
        log.info("Deleting actor statuses: {}", actorId);
        return Single.fromPublisher(actorStatusCollection.deleteMany(eq("actorId", actorId)));
    }
}
