package server.util;

import static com.mongodb.client.model.Filters.ne;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.util.List;
import server.configuration.MongoConfiguration;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.motion.model.PlayerMotionList;

@Singleton
public class PlayerMotionUtil {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<PlayerMotion> motionCollection;
    MongoCollection<Monster> monsterMongoCollection;

    public PlayerMotionUtil(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public void deleteAllPlayerMotionData() {
        Single.fromPublisher(motionCollection.deleteMany(ne("actorId", "deleteAll"))).blockingGet();
    }

    public void deleteMotionForPlayers(List<String> actorIds) {
        Single.fromPublisher(motionCollection.deleteMany(Filters.all("actorId", actorIds)))
                .blockingGet();
    }

    public void deleteAllMobInstanceData() {
        Single.fromPublisher(monsterMongoCollection.deleteMany(ne("actorId", "deleteAll")))
                .blockingGet();
    }

    public void deleteMobInstancesWithIds(List<String> actorIds) {
        Single.fromPublisher(
                        monsterMongoCollection.deleteMany(
                                Filters.all("actorId", actorIds)))
                .blockingGet();
    }

    public static boolean playerMotionListEquals(PlayerMotionList pml1, PlayerMotionList pml2) {
        if (pml1.getPlayerMotionList().size() != pml2.getPlayerMotionList().size()) {
            return false;
        }
        List<PlayerMotion> list1 = pml1.getPlayerMotionList();
        List<PlayerMotion> list2 = pml2.getPlayerMotionList();

        for (int i = 0; i < list1.size(); i++) {
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
                && pm1.getActorId().equals(pm2.getActorId());
    }

    private void prepareCollections() {
        this.motionCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getPlayerMotion(), PlayerMotion.class);
        this.monsterMongoCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getMobInstance(), Monster.class);
    }
}
