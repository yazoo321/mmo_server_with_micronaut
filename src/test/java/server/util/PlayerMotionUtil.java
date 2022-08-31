package server.util;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Single;
import server.configuration.MongoConfiguration;
import server.player.motion.dto.PlayerMotion;

import javax.inject.Singleton;

import static com.mongodb.client.model.Filters.ne;

@Singleton
public class PlayerMotionUtil {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<PlayerMotion> motionCollection;

    public PlayerMotionUtil(
            MongoConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public void deleteAllPlayerMotionData() {
        Single.fromPublisher(motionCollection.deleteMany(
                ne("playerName", "deleteAll")
        )).blockingGet();
    }

    private void prepareCollections() {
        this.motionCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getPlayerMotion(), PlayerMotion.class);

    }

}
