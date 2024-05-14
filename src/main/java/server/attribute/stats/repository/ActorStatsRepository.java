package server.attribute.stats.repository;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import server.attribute.stats.model.Stats;
import server.common.configuration.MongoConfiguration;

import static com.mongodb.client.model.Filters.eq;

@Slf4j
@Singleton
@CacheConfig("actor-stats-cache")
public class ActorStatsRepository {

    private static final String ACTOR_STATS_CACHE = "actor-stats-cache";

    private final MongoConfiguration configuration;
    private final MongoClient mongoClient;
    private final MongoCollection<Stats> actorStats;

    public ActorStatsRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        this.actorStats = getCollection();
    }

    @Cacheable(value = ACTOR_STATS_CACHE, parameters = "actorId")
    public Single<Stats> fetchActorStats(String actorId) {
        return Single.fromPublisher(actorStats.find(eq("actorId", actorId)));
    }

    @CachePut(value = ACTOR_STATS_CACHE, parameters = "actorId", async = true)
    public Single<Stats> updateStats(String actorId, Stats stats) {
        Bson filter = Filters.eq("actorId", stats.getActorId());
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        return Single.fromPublisher(actorStats.replaceOne(filter, stats, options))
                .map(res -> stats);
    }

    @CacheInvalidate(value = ACTOR_STATS_CACHE, parameters = "actorId", async = true)
    public Single<DeleteResult> deleteStats(String actorId) {
        return Single.fromPublisher(actorStats.deleteOne(eq("actorId", actorId)));
    }

    private MongoCollection<Stats> getCollection() {
        return mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getActorStats(), Stats.class);
    }
}
