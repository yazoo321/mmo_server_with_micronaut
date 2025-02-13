package server.skills.repository;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import server.common.configuration.MongoConfiguration;
import server.skills.model.ActorSkills;

@Slf4j
@Singleton
public class ActorSkillsRepository {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<ActorSkills> actorSkillsCollection;

    public ActorSkillsRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
    }

    public Single<ActorSkills> getActorSkills(String actorId) {
        return Single.fromPublisher(actorSkillsCollection.find(eq("actorId", actorId)))
                .doOnError((exception) -> log.error("actor skills not found for {}", actorId));
    }

    public Single<ActorSkills> setActorSkills(ActorSkills actorSkills) {
        return Single.fromPublisher(
                        actorSkillsCollection.replaceOne(
                                eq("actorId", actorSkills.getActorId()), actorSkills))
                .map(r -> actorSkills);
    }

    public Single<DeleteResult> deleteActorSkills(String actorId) {
        return Single.fromPublisher(actorSkillsCollection.deleteOne(eq("actorId", actorId)));
    }

    @PostConstruct
    private void prepareCollections() {
        this.actorSkillsCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getActorSkills(), ActorSkills.class);
    }
}
