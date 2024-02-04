package server.attribute.stats.helpers;

import static com.mongodb.client.model.Filters.ne;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import server.attribute.stats.model.Stats;
import server.common.configuration.MongoConfiguration;

@Singleton
public class StatsTestHelper {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Stats> statsCollection;

    public StatsTestHelper(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public void deleteAllAttributeData() {
        Single.fromPublisher(statsCollection.deleteMany(ne("actorId", "deleteAll"))).blockingGet();
    }

    private void prepareCollections() {
        this.statsCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getActorStats(), Stats.class);
    }
}
