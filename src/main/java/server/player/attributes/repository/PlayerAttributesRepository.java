package server.player.attributes.repository;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import server.configuration.MongoConfiguration;
import server.player.attributes.model.PlayerAttributes;

import javax.inject.Singleton;

import java.util.NoSuchElementException;

import static com.mongodb.client.model.Filters.eq;

@Slf4j
@Singleton
public class PlayerAttributesRepository {
    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<PlayerAttributes> playerAttributesCollection;

    public PlayerAttributesRepository(
            MongoConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        this.playerAttributesCollection = getCollection();
    }


    public PlayerAttributes findPlayerAttributes(String playerName) {
        return Single.fromPublisher(
                playerAttributesCollection.find(
                        eq("playerName", playerName)
                )
        ).blockingGet();
    }

    public void insertPlayerAttributes(PlayerAttributes attributes) {
        try {
            findPlayerAttributes(attributes.getPlayerName());
            log.error("Was not expecting to find player attributes for character {}", attributes.getPlayerName());
        } catch (NoSuchElementException e) {
            // expected to not find one
            Single.fromPublisher(
                    playerAttributesCollection.insertOne(attributes)
            ).blockingGet();
        }
    }

    public PlayerAttributes updatePlayerAttributes(String playerName, PlayerAttributes attributes) {
        return Single.fromPublisher(
                playerAttributesCollection
                        .findOneAndReplace(
                                eq("playerName", playerName), attributes
                        )
        ).blockingGet();
    }

    public void deletePlayerAttributes(String playerName) {
        // This should be used cautiously, e.g. when rolling back changes

        Single.fromPublisher(
                playerAttributesCollection
                        .deleteOne(
                                eq("playerName", playerName)
                        )
        ).blockingGet();
    }

    private MongoCollection<PlayerAttributes> getCollection() {
        return mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getPlayerAttributes(), PlayerAttributes.class);
    }
}
