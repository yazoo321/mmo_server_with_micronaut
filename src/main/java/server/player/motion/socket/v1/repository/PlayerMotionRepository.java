package server.player.motion.socket.v1.repository;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Location;
import server.common.mongo.query.MongoDbQueryHelper;
import server.configuration.MongoConfiguration;
import server.items.dropped.model.DroppedItem;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.model.exceptions.ItemException;
import server.player.motion.dto.PlayerMotion;
import server.player.motion.dto.exceptions.PlayerMotionException;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.mongodb.client.model.Filters.*;

@Slf4j
@Singleton
public class PlayerMotionRepository {

    private final static Integer DROPPED_ITEM_TIMEOUT_SECONDS = 60;

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<PlayerMotion> playerMotionMongoCollection;

    public PlayerMotionRepository(
            MongoConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public PlayerMotion findPlayerMotion(String playerName) {
        try {
            return Single.fromPublisher(
                    playerMotionMongoCollection.find(
                            eq("playerName", playerName)
                    )
            ).blockingGet();
        } catch (NoSuchElementException e) {
            log.error("Player motion not found for {}", playerName);

            throw new PlayerMotionException("Failed to find player motion");
        }
    }

    public PlayerMotion insertPlayerMotion(PlayerMotion playerMotion) {
        try {
            return findPlayerMotion(playerMotion.getPlayerName());
        } catch (PlayerMotionException e) {
            Single.fromPublisher(
                    playerMotionMongoCollection.insertOne(playerMotion)
            ).blockingGet();
        }

        return playerMotion;
    }

    public PlayerMotion updatePlayerMotion(PlayerMotion playerMotion) {
        // allows us to update player motion async, we can consider doing this less often if working primarily from cache
        Single.fromPublisher(
                playerMotionMongoCollection.findOneAndReplace(
                        eq("playerName", playerMotion.getPlayerName()),
                        playerMotion
                )
        ).subscribe();

        return playerMotion;
    }




    private void prepareCollections() {
        this.playerMotionMongoCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getItemInstancesCollection(), PlayerMotion.class);
    }
}
