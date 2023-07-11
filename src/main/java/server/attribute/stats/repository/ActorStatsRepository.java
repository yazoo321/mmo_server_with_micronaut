package server.attribute.stats.repository;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import server.attribute.stats.model.Stats;
import server.configuration.MongoConfiguration;

@Slf4j
@Singleton
public class ActorStatsRepository {
    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Stats> actorStats;

    public ActorStatsRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        this.actorStats = getCollection();
    }

    public Single<Stats> findActorStats(String actorId) {
        return Single.fromPublisher(actorStats.find(eq("actorId", actorId)));
    }

    public Single<Stats> updateStats(Stats stats) {
        Bson filter = Filters.eq("actorId", stats.getActorId());
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        return Single.fromPublisher(actorStats.replaceOne(filter, stats, options))
                .map(res -> stats);
    }

    public Single<DeleteResult> deleteAttributes(String actorId) {
        return Single.fromPublisher(actorStats.deleteOne(eq("actorId", actorId)));
    }

    private MongoCollection<Stats> getCollection() {
        return mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getActorStats(), Stats.class);
    }
}
