package server.actionbar.repository;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import server.actionbar.model.ActorActionbar;
import server.common.configuration.MongoConfiguration;
import server.session.SessionParamHelper;

@Slf4j
@Singleton
public class ActionbarRepository {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<ActorActionbar> actionbarMongoCollection;

    @Inject SessionParamHelper sessionParamHelper;

    public ActionbarRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public Single<List<ActorActionbar>> getActorActionbar(String actorId) {
        return Flowable.fromPublisher(actionbarMongoCollection.find(eq("actorId", actorId)))
                .toList()
                .doOnError((exception) -> log.error("actor skills not found for {}", actorId));
    }

    public void updateActorActionbar(ActorActionbar actorActionbar) {
        Bson filter =
                Filters.and(
                        eq("actorId", actorActionbar.getActorId()),
                        eq("actionbarId", actorActionbar.getActionbarId()));

        ReplaceOptions options = new ReplaceOptions().upsert(true);
        Single.fromPublisher(actionbarMongoCollection.replaceOne(filter, actorActionbar, options))
                .subscribe();
    }

    public void deleteActorActionbar(String actorId, String actionId) {
        Single.fromPublisher(
                        actionbarMongoCollection.findOneAndDelete(
                                and(eq("actorId", actorId), eq("actionbarId", actionId))))
                .subscribe();
    }

    public Single<DeleteResult> deleteActorActionbar(String actorId) {
        return Single.fromPublisher(
                actionbarMongoCollection.deleteMany(eq("actorId", actorId))
        );
    }

    private void prepareCollections() {
        this.actionbarMongoCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getActionbar(), ActorActionbar.class);
    }
}
