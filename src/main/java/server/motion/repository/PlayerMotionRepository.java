package server.motion.repository;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
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

    public Single<PlayerMotion> insertPlayerMotion(PlayerMotion playerMotion) {
        return Single.fromPublisher(playerMotionMongoCollection.insertOne(playerMotion))
                .map(success -> playerMotion);
    }

    public Single<PlayerMotion> updatePlayerMotion(PlayerMotion playerMotion) {
        // allows us to update player motion async, we can consider doing this less often if working
        // primarily from cache
        return Single.fromPublisher(
                playerMotionMongoCollection.findOneAndReplace(
                        eq("playerName", playerMotion.getPlayerName()), playerMotion));
    }

    public void deletePlayerMotion(String playerName) {
        Single.fromPublisher(playerMotionMongoCollection.deleteOne(eq("playerName", playerName)))
                .blockingGet();
    }

    public List<PlayerMotion> getPlayersNearby(PlayerMotion playerMotion) {
        return MongoDbQueryHelper.nearbyMotionFinder(
                playerMotionMongoCollection, playerMotion, 1000);
    }

    public Single<List<PlayerMotion>> getPlayersNearbyAsync(PlayerMotion playerMotion, Integer threshold) {
        return MongoDbQueryHelper.getNearbyPlayers(
                playerMotionMongoCollection, playerMotion, threshold);
    }

    public UpdateResult checkAndUpdateUserOnline() {
        // Duplicate functionality of Character service
        LocalDateTime logoutTime = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(10);

        // if is online and not updated in the last 20 seconds, set to logged out.
        return Flowable.fromPublisher(
                        playerMotionMongoCollection.updateMany(
                                combine(eq("isOnline", true), lt("updatedAt", logoutTime)),
                                set("isOnline", false)))
                .firstElement()
                .blockingGet();
    }

    private void prepareCollections() {
        this.playerMotionMongoCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getPlayerMotion(), PlayerMotion.class);
    }
}
