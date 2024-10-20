package server.attribute.status.helpers;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import org.bson.conversions.Bson;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.common.configuration.MongoConfiguration;

import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.ne;

@Singleton
public class StatusTestHelper {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Status> statusCollection;

    public StatusTestHelper(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public void resetStatuses(List<String> actors) {
        Bson filter = in("actorId", actors);
        Single.fromPublisher(statusCollection.updateMany(
                filter,
                Updates.combine(
                        Updates.set("actorStatuses", Set.of()),
                Updates.set("statusEffects",  Set.of())
        ))).blockingSubscribe();
    }

    private void prepareCollections() {
        this.statusCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getActorStatus(), Status.class);
    }
}
