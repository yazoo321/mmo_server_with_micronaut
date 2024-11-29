package server.combat.repository;

import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import server.combat.model.ActorThreat;
import server.common.configuration.MongoConfiguration;
import server.motion.dto.PlayerMotion;
import server.motion.dto.exceptions.PlayerMotionException;
import server.motion.repository.ActorMotionRepository;

import javax.annotation.PostConstruct;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

@Slf4j
@Singleton
@CacheConfig("actor-threat-cache")
public class ActorThreatRepository {
    private static final String ACTOR_THREAT_CACHE = "actor-threat-cache";

    private final MongoConfiguration configuration;
    private final MongoClient mongoClient;
    private MongoCollection<ActorThreat> actorThreatCollection;

    public ActorThreatRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
    }

    @PostConstruct
    private void prepareCollections() {
        this.actorThreatCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getActorThreat(), ActorThreat.class);
    }

    @Cacheable(value = ACTOR_THREAT_CACHE, parameters = "actorId")
    public Single<ActorThreat> fetchActorThreat(String actorId) {
        return Single.fromPublisher(actorThreatCollection.find(eq("actorId", actorId)))
                .doOnError((err) -> log.error(err.getMessage()));
    }

    @CachePut(value = ACTOR_THREAT_CACHE, parameters = "actorId")
    public Single<ActorThreat> upsertActorThreat(String actorId, ActorThreat actorThreat) {
        Bson filter = Filters.eq("actorId", actorId);
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        return Single.fromPublisher(actorThreatCollection.replaceOne(filter, actorThreat, options))
                .map(res -> actorThreat);
    }

    @CachePut(value = ACTOR_THREAT_CACHE, parameters = "actorId")
    public Single<ActorThreat> addThreatToActor(String actorId, String targetId, Integer threat) {
        // Create filter to find the document
        Bson filter = Filters.eq("actorId", actorId);

        // Increment the threat level for the targetId
        Bson update = Updates.inc("actorThreat." + targetId, threat);

        // Use findOneAndUpdate to perform the update and fetch the latest document
        return Single.fromPublisher(
                        actorThreatCollection.findOneAndUpdate(
                                filter,
                                update,
                                new FindOneAndUpdateOptions()
                                        .upsert(true) // Insert if document doesn't exist
                                        .returnDocument(ReturnDocument.AFTER) // Return updated document
                        )
                )
                .map(document -> {
                    if (document == null) {
                        throw new RuntimeException("Failed to update or retrieve the actor threat data.");
                    }
                    return document; // Assuming document is directly mapped to ActorThreat
                });
    }

    public Single<List<ActorThreat>> updateActorThreat(List<ActorThreat> actorThreats) {
        if (actorThreats == null || actorThreats.isEmpty()) {
            return Single.just(Collections.emptyList());
        }

        return Flowable.fromIterable(actorThreats) // Convert list to a Flowable to process each ActorThreat
                .flatMapSingle(actorThreat -> {
                    String actorId = actorThreat.getActorId(); // Assuming ActorThreat has a getActorId() method
                    Bson filter = Filters.eq("actorId", actorId);
                    ReplaceOptions options = new ReplaceOptions().upsert(true);

                    // Upsert each ActorThreat and cache it
                    return Single.fromPublisher(actorThreatCollection.replaceOne(filter, actorThreat, options))
                            .map(res -> actorThreat) // Return the ActorThreat after successful upsert
                            .doOnSuccess(updated -> cacheActorThreat(actorId, updated)) // Update the cache
                            .doOnError(err -> log.error("Failed to update ActorThreat for actorId: " + actorId, err));
                })
                .toList(); // Collect all the results into a Single<List<ActorThreat>>
    }

    @CachePut(value = ACTOR_THREAT_CACHE, parameters = "actorId")
    void cacheActorThreat(String actorId, ActorThreat actorThreat) {
        // This method exists solely for the @CachePut annotation to trigger cache updates.
    }

    public Single<List<ActorThreat>> fetchActorThreat(Set<String> actorIds) {
        return Flowable.fromPublisher(actorThreatCollection.find(in("actorId", actorIds))).toList();
    }

    @CacheInvalidate(value = ACTOR_THREAT_CACHE, parameters = "actorId")
    public Single<DeleteResult> resetActorThreat(String actorId) {
        return Single.fromPublisher(actorThreatCollection.deleteOne(eq("actorId", actorId)));
    }

}
