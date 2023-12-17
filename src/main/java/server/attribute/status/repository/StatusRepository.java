package server.attribute.status.repository;

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
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.configuration.MongoConfiguration;

import static com.mongodb.client.model.Filters.eq;

@Slf4j
@Singleton
public class StatusRepository {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<ActorStatus> actorStatusCollection;


    public StatusRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        this.actorStatusCollection = getCollection();
    }

    private MongoCollection<ActorStatus> getCollection() {
        return mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getActorStatus(), ActorStatus.class);
    }

    public Single<ActorStatus> getActorStatuses(String actorId) {
        return Single.fromPublisher(actorStatusCollection.find(eq("actorId", actorId)));
    }

    public Single<ActorStatus> updateStatus(ActorStatus actorStatus) {
        Bson filter = Filters.eq("actorId", actorStatus.getActorId());
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        return Single.fromPublisher(actorStatusCollection.replaceOne(filter, actorStatus, options))
                .map(res -> actorStatus);
    }

    public Single<DeleteResult> deleteActorStatuses(String actorId) {
        return Single.fromPublisher(actorStatusCollection.deleteOne(eq("actorId", actorId)));
    }

}
