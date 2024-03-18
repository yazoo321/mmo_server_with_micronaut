package server.actionbar.repository;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.actionbar.model.ActionbarContent;
import server.actionbar.model.ActorActionbar;
import server.common.configuration.MongoConfiguration;
import server.session.SessionParamHelper;
import server.skills.model.ActorSkills;
import server.skills.model.Skill;

import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

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
        Single.fromPublisher(
                actionbarMongoCollection.findOneAndUpdate(
                        and(eq("actorId", actorActionbar.getActorId()),
                                eq("actionbarId", actorActionbar.getActionbarId())),
                        set("actionbarContent", actorActionbar.getActionbarContent())))
                .subscribe();
    }

    public void deleteActorActionbar(String actorId, String actionId) {
        Single.fromPublisher(
                actionbarMongoCollection.findOneAndDelete(and(eq("actorId", actorId),
                        eq("actionbarId", actionId)))).subscribe();
    }

    private void prepareCollections() {
        this.actionbarMongoCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getActionbar(), ActorActionbar.class);
    }
}
