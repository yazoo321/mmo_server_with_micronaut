package server.attribute.status.helpers;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bson.conversions.Bson;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.repository.StatusRepository;
import server.common.configuration.MongoConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.ne;

@Singleton
public class StatusTestHelper {

//    MongoConfiguration configuration;
//    MongoClient mongoClient;
//    MongoCollection<ActorStatus> statusCollection;

    @Inject
    StatusRepository statusRepository;

//    public StatusTestHelper(MongoConfiguration configuration, MongoClient mongoClient) {
//        this.configuration = configuration;
//        this.mongoClient = mongoClient;
//        prepareCollections();
//    }

    public void resetStatuses(List<String> actors) {
        actors.forEach(actor -> statusRepository.deleteActorStatuses(actor).blockingSubscribe());

        actors.forEach(actor -> {
            ActorStatus defaultActorStatus = new ActorStatus();
            defaultActorStatus.setActorId(actor);
            defaultActorStatus.setActorStatuses(new HashSet<>());
            defaultActorStatus.setStatusEffects(new HashSet<>());

            statusRepository.updateStatus(actor, defaultActorStatus).blockingSubscribe();
        });
//        Bson filter = in("actorId", actors);
//        Single.fromPublisher(statusCollection.deleteMany(
//                filter
//                )).blockingSubscribe();
//        actors.forEach(actor -> {
//            ActorStatus defaultActorStatus = new ActorStatus();
//            defaultActorStatus.setActorId(actor);
//            defaultActorStatus.setActorStatuses(new HashSet<>());
//            defaultActorStatus.setStatusEffects(new HashSet<>());
//
//            Single.fromPublisher(statusCollection.insertOne(defaultActorStatus)).blockingSubscribe();
//        });
//
//        List<ActorStatus> statuses = Flowable.fromPublisher(statusCollection.find(filter)).toList().blockingGet();
//        Single.fromPublisher(statusCollection.updateMany(
//                filter,
//                Updates.combine(
//                        Updates.set("actorStatuses", Set.of()),
//                        Updates.set("statusEffects",  Set.of())
//                ))).blockingSubscribe();
//        Bson filter = in("actorId", actors);
//        Single.fromPublisher(statusCollection.updateMany(
//                filter,
//                Updates.combine(
//                        Updates.set("actorStatuses", Set.of()),
//                Updates.set("statusEffects",  Set.of())
//        ))).blockingSubscribe();
    }

//    private void prepareCollections() {
//        this.statusCollection =
//                mongoClient
//                        .getDatabase(configuration.getDatabaseName())
//                        .getCollection(configuration.getActorStatus(), ActorStatus.class);
//
//
//    }
}
