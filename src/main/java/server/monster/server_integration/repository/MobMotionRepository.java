package server.monster.server_integration.repository;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import server.common.dto.Location;
import server.common.mongo.query.MongoDbQueryHelper;
import server.configuration.MongoConfiguration;
import server.monster.server_integration.model.MobMotion;
import server.monster.server_integration.model.exceptions.MobMotionException;

@Slf4j
@Repository
public class MobMotionRepository {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<MobMotion> mobMotionMongoCollection;

    public MobMotionRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public Single<MobMotion> findMobMotion(String mobInstanceId) {
        try {
            return Single.fromPublisher(
                    mobMotionMongoCollection.find(eq("mobInstanceId", mobInstanceId)));
        } catch (NoSuchElementException e) {
            log.error("Mob with instance id {} not found", mobInstanceId);

            throw new MobMotionException("Failed to find mob motion");
        }
    }

    public MobMotion insertMobMotion(MobMotion mobMotion) {
        try {
            return findMobMotion(mobMotion.getMobInstanceId()).blockingGet();
        } catch (MobMotionException e) {
            InsertOneResult res =
                    Single.fromPublisher(mobMotionMongoCollection.insertOne(mobMotion))
                            .blockingGet();

            if (res.wasAcknowledged()) {
                return mobMotion;
            } else {
                throw new MobMotionException("Failed to insert mob motion");
            }
        }
    }

    public Single<MobMotion> updateMobMotion(MobMotion mobMotion) {
        return Single.fromPublisher(
                mobMotionMongoCollection.findOneAndReplace(
                        eq("mobInstanceId", mobMotion.getMobInstanceId()), mobMotion));
    }

    public Single<DeleteResult> deleteMobInstance(String mobInstanceId) {
        return Single.fromPublisher(
                mobMotionMongoCollection.deleteOne(eq("mobInstanceId", mobInstanceId)));
    }

    public Single<List<MobMotion>> getMobsNearby(Location location) {
        return MongoDbQueryHelper.nearbyMobMotionFinder(mobMotionMongoCollection, location, 1000);
    }

    private void prepareCollections() {
        this.mobMotionMongoCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getMobMotion(), MobMotion.class);
    }
}
