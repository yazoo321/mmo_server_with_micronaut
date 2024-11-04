package server.faction.repository;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import server.common.configuration.MongoConfiguration;
import server.faction.model.ActorAllegiance;
import server.items.inventory.model.CharacterItem;
import server.items.inventory.model.Inventory;

import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;

@CacheConfig("actorAllegianceCache")
@Singleton
@Slf4j
public class ActorAllegianceRepository {

    private static final String ACTOR_ALLEGIANCE_CACHE = "actor-allegiance-cache";

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<ActorAllegiance> actorAllegianceCollection;

    public ActorAllegianceRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }


    @Cacheable(value = ACTOR_ALLEGIANCE_CACHE, parameters = "actorId")
    public Single<List<ActorAllegiance>> findByActorId(String actorId) {
        return Flowable.fromPublisher(actorAllegianceCollection.find(eq("actorId", actorId)))
                .toList();
    }

    @CachePut(value = ACTOR_ALLEGIANCE_CACHE, parameters = "actorId", async = true)
    public Single<ActorAllegiance> insert(String actorId, String allegianceName) {
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        Bson filter = and(
                eq("actorId", actorId),
                eq("allegianceName", allegianceName));

        ActorAllegiance actorAllegiance = new ActorAllegiance(actorId, allegianceName);
        return Single.fromPublisher(actorAllegianceCollection.replaceOne(
                        filter, actorAllegiance, options)).doOnError(err -> log.error(err.getMessage()))
                .map(res -> actorAllegiance);
    }

    private void prepareCollections() {
        this.actorAllegianceCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getInventoryCollection(), ActorAllegiance.class);
    }
}
