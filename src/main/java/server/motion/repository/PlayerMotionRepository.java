package server.motion.repository;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import server.common.mongo.query.MongoDbQueryHelper;
import server.configuration.MongoConfiguration;
import server.motion.dto.PlayerMotion;
import server.motion.dto.exceptions.PlayerMotionException;

@Slf4j
@Singleton
public class PlayerMotionRepository {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<PlayerMotion> playerMotionMongoCollection;

    public PlayerMotionRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public Single<PlayerMotion> findPlayerMotion(String playerName) {
        return Single.fromPublisher(playerMotionMongoCollection.find(eq("playerName", playerName)))
                .doOnError(
                        (exception) -> {
                            log.error("Player motion not found for {}", playerName);
                            throw new PlayerMotionException("Failed to find player motion");
                        });
    }

    public Single<List<PlayerMotion>> findPlayersMotion(Set<String> playerName) {
        return Flowable.fromPublisher(
                        playerMotionMongoCollection.find(in("playerName", playerName)))
                .toList();
    }

    public Single<PlayerMotion> insertPlayerMotion(PlayerMotion playerMotion) {
        return Single.fromPublisher(playerMotionMongoCollection.insertOne(playerMotion))
                .map(success -> playerMotion);
    }

    public Single<PlayerMotion> setPlayerOnline(String playerName) {
        return Single.fromPublisher(
                playerMotionMongoCollection.findOneAndUpdate(
                        eq("playerName"), set("isOnline", true)));
    }

    public Single<PlayerMotion> updateMotion(PlayerMotion playerMotion) {
        return Single.fromPublisher(
                        playerMotionMongoCollection.findOneAndUpdate(
                                eq("playerName", playerMotion.getPlayerName()),
                                Updates.combine(
                                        Updates.set("motion", playerMotion.getMotion()),
                                        Updates.set("isOnline", true),
                                        Updates.set(
                                                "updatedAt",
                                                Instant.now().truncatedTo(ChronoUnit.MICROS)))))
                .map(success -> playerMotion);
    }

    public void deletePlayerMotion(String playerName) {
        Single.fromPublisher(playerMotionMongoCollection.deleteOne(eq("playerName", playerName)))
                .blockingGet();
    }

    public List<PlayerMotion> getPlayersNearby(PlayerMotion playerMotion) {
        return MongoDbQueryHelper.nearbyMotionFinder(
                playerMotionMongoCollection, playerMotion, 1000);
    }

    public Single<List<PlayerMotion>> getPlayersNearbyAsync(
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
