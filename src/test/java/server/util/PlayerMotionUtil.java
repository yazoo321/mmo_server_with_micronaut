package server.util;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Single;
import server.configuration.MongoConfiguration;
import server.player.motion.dto.PlayerMotion;
import server.player.motion.socket.v1.model.PlayerMotionList;

import javax.inject.Singleton;

import java.util.List;

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


    public static boolean playerMotionListEquals(PlayerMotionList pml1, PlayerMotionList pml2) {
        if (pml1.getPlayerMotionList().size() != pml2.getPlayerMotionList().size()) {
            return false;
        }
        List<PlayerMotion> list1 = pml1.getPlayerMotionList();
        List<PlayerMotion> list2 = pml2.getPlayerMotionList();

        for(int i = 0; i < list1.size(); i++) {
            if (!playerMotionEquals(list1.get(i), list2.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean playerMotionEquals(PlayerMotion pm1, PlayerMotion pm2) {
        // Compare all fields except updated at
        return pm1.getMotion().equals(pm2.getMotion())
                && pm1.getIsOnline().equals(pm2.getIsOnline())
                && pm1.getPlayerName().equals(pm2.getPlayerName());
    }

    private void prepareCollections() {
        this.motionCollection = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getPlayerMotion(), PlayerMotion.class);

    }

}
