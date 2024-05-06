package server.motion.repository;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CachePut;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import server.common.configuration.MongoConfiguration;
import server.common.dto.Motion;
import server.common.mongo.query.MongoDbQueryHelper;
import server.motion.dto.PlayerMotion;
import server.motion.dto.exceptions.PlayerMotionException;

@Slf4j
@Singleton
@CacheConfig("actor-motion-cache")
public class PlayerMotionRepository {

    private static final String ACTOR_MOTION_CACHE = "actor-motion-cache";
    private final MongoConfiguration configuration;
    private final MongoClient mongoClient;
    private MongoCollection<PlayerMotion> playerMotionMongoCollection;

    public PlayerMotionRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public Single<PlayerMotion> fetchPlayerMotion(String actorId) {
        return Single.fromPublisher(playerMotionMongoCollection.find(eq("actorId", actorId)))
                .doOnError(
                        (err) -> {
                            log.error("Player motion not found for {}", actorId);
                            throw new PlayerMotionException("Failed to find player motion");
                        });
    }

    public Single<List<PlayerMotion>> fetchPlayersMotion(Set<String> actorId) {
        return Flowable.fromPublisher(playerMotionMongoCollection.find(in("actorId", actorId)))
                .toList();
    }

    @CachePut(value = ACTOR_MOTION_CACHE, parameters = "actorId")
    public Single<Motion> insertPlayerMotion(String actorId, PlayerMotion playerMotion) {
        return Single.fromPublisher(playerMotionMongoCollection.insertOne(playerMotion))
                .map(success -> playerMotion.getMotion());
    }

    public Single<PlayerMotion> setPlayerOnlineStatus(String actorId, boolean isOnline) {
        return Single.fromPublisher(
                playerMotionMongoCollection.findOneAndUpdate(
                        eq("actorId", actorId), set("isOnline", isOnline)));
    }

    public Single<PlayerMotion> updateMotion(PlayerMotion playerMotion) {
        return Single.fromPublisher(
                        playerMotionMongoCollection.findOneAndUpdate(
                                eq("actorId", playerMotion.getActorId()),
                                Updates.combine(
                                        Updates.set("motion", playerMotion.getMotion()),
                                        Updates.set("isOnline", true),
                                        Updates.set(
                                                "updatedAt",
                                                Instant.now().truncatedTo(ChronoUnit.MICROS)))))
                .map(success -> playerMotion);
    }

    public Single<DeleteResult> deletePlayerMotion(String actorId) {
        return Single.fromPublisher(playerMotionMongoCollection.deleteOne(eq("actorId", actorId)));
    }

    public Single<List<PlayerMotion>> getPlayersNearby(
            PlayerMotion playerMotion, Integer threshold) {
        return MongoDbQueryHelper.getNearbyPlayers(
                playerMotionMongoCollection, playerMotion, threshold);
    }

    public Flowable<UpdateResult> checkAndUpdateUserOnline() {
        // Duplicate functionality of Character service
        Instant logoutTime = Instant.now().minusSeconds(300);

        // if is online and not updated in the last 20 seconds, set to logged out.
        return Flowable.fromPublisher(
                playerMotionMongoCollection.updateMany(
                        combine(eq("isOnline", true), lt("updatedAt", logoutTime)),
                        set("isOnline", false)));
    }

    private void prepareCollections() {
        this.playerMotionMongoCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getPlayerMotion(), PlayerMotion.class);
    }
}
