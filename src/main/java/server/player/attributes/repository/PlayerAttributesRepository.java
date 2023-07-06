package server.player.attributes.repository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import server.configuration.MongoConfiguration;
import server.player.attributes.model.PlayerAttributes;

@Slf4j
@Singleton
@Deprecated // use Stats instead
public class PlayerAttributesRepository {
    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<PlayerAttributes> playerAttributesCollection;

    public PlayerAttributesRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        this.playerAttributesCollection = getCollection();
    }

    public PlayerAttributes findPlayerAttributes(String playerName) {
        return Single.fromPublisher(playerAttributesCollection.find(eq("playerName", playerName)))
                .blockingGet();
    }

    public void insertPlayerAttributes(PlayerAttributes attributes) {
        try {
            findPlayerAttributes(attributes.getPlayerName());
            log.error(
                    "Was not expecting to find player attributes for character {}",
                    attributes.getPlayerName());
        } catch (NoSuchElementException e) {
            // expected to not find one
            Single.fromPublisher(playerAttributesCollection.insertOne(attributes)).blockingGet();
        }
    }

    public PlayerAttributes updatePlayerAttributes(String playerName, PlayerAttributes attributes) {
        return Single.fromPublisher(
                        playerAttributesCollection.findOneAndReplace(
                                eq("playerName", playerName), attributes))
                .blockingGet();
    }

    public Single<Map<String, Integer>> updateCurrentAttributes(
            String playerName, Map<String, Integer> currentAttributes) {
        return Single.fromPublisher(
                        playerAttributesCollection.updateOne(
                                eq("playerName", playerName),
                                set("currentAttributes", currentAttributes)))
                .map(res -> currentAttributes);
    }

    public void deletePlayerAttributes(String playerName) {
        // This should be used cautiously, e.g. when rolling back changes

        Single.fromPublisher(playerAttributesCollection.deleteOne(eq("playerName", playerName)))
                .blockingGet();
    }

    private MongoCollection<PlayerAttributes> getCollection() {
        return mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getPlayerAttributes(), PlayerAttributes.class);
    }
}
