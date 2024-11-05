package server.faction.repository;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.Cacheable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import server.common.configuration.MongoConfiguration;
import server.faction.model.HostileAllegiance;

@CacheConfig("actorAllegianceCacheLocal")
@Singleton
@Slf4j
public class HostileAllegianceRepository {

    private static final String HOSTILE_ALLEGIANCE_CACHE = "hostile-allegiance-cache";

    private final MongoClient mongoClient;
    private final MongoConfiguration configuration;
    private MongoCollection<HostileAllegiance> hostileAllegianceCollection;

    public HostileAllegianceRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        //        prepareCollections();
    }

    @Cacheable(value = HOSTILE_ALLEGIANCE_CACHE, parameters = "allegianceName")
    public Single<List<HostileAllegiance>> findHostilitiesByAllegiance(
            List<String> allegianceName) {
        return Flowable.fromPublisher(
                        hostileAllegianceCollection.find(
                                or(
                                        in("hostileTo", allegianceName),
                                        in("allegianceName", allegianceName))))
                .toList();
    }

    @CacheInvalidate(value = HOSTILE_ALLEGIANCE_CACHE, parameters = "allegianceName", async = true)
    public Single<HostileAllegiance> upsertHostility(
            String allegianceName, String hostileTo, int hostilityLevel) {
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        Bson filter = and(eq("allegianceName", allegianceName), eq("hostileTo", hostileTo));

        HostileAllegiance hostileAllegiance =
                new HostileAllegiance(allegianceName, hostileTo, hostilityLevel);
        return Single.fromPublisher(
                        hostileAllegianceCollection.replaceOne(filter, hostileAllegiance, options))
                .doOnError(err -> log.error(err.getMessage()))
                .map(res -> hostileAllegiance);
    }

    public Single<DeleteResult> delete(String allegianceName, String hostileTo) {
        return Single.fromPublisher(
                hostileAllegianceCollection.deleteOne(
                        and(eq("allegianceName", allegianceName), eq("hostileTo", hostileTo))));
    }

    @PostConstruct
    private void prepareCollections() {
        this.hostileAllegianceCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection("hostile_allegiances", HostileAllegiance.class);
    }
}
