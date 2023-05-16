package server.monster.attributes.repository;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.util.Map;
import server.configuration.MongoConfiguration;
import server.monster.attributes.model.MonsterAttributes;

@Singleton
public class MonsterAttributeRepository {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<MonsterAttributes> monsterAttributesMongoCollection;

    public MonsterAttributeRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        this.monsterAttributesMongoCollection = getCollection();
    }

    public Single<MonsterAttributes> insertMonsterAttributes(MonsterAttributes mobAttributes) {
        return Single.fromPublisher(monsterAttributesMongoCollection.insertOne(mobAttributes))
                .map(success -> mobAttributes);
    }

    public Single<MonsterAttributes> updateMobCurrentAttributes(
            String mobInstanceId, Map<String, Integer> currentAttributes) {
        return Single.fromPublisher(
                monsterAttributesMongoCollection.findOneAndUpdate(
                        eq("mobInstanceId", mobInstanceId),
                        Updates.set("currentAttributes", currentAttributes)));
    }

    public Single<DeleteResult> deleteMonsterAttributes(String mobInstanceId) {
        return Single.fromPublisher(
                monsterAttributesMongoCollection.deleteOne(eq("mobInstanceId", mobInstanceId)));
    }

    private MongoCollection<MonsterAttributes> getCollection() {
        return mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getMobAttributes(), MonsterAttributes.class);
    }
}
