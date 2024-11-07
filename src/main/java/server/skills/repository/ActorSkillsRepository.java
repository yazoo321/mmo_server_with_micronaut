package server.skills.repository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import server.common.configuration.MongoConfiguration;
import server.session.SessionParamHelper;
import server.skills.model.ActorSkills;
import server.skills.model.Skill;

import javax.annotation.PostConstruct;

@Slf4j
@Singleton
public class ActorSkillsRepository {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<ActorSkills> actorSkillsCollection;

    @Inject SessionParamHelper sessionParamHelper;

    public ActorSkillsRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
    }

    public Single<ActorSkills> getActorSkills(String actorId) {
        return Single.fromPublisher(actorSkillsCollection.find(eq("actorId", actorId)))
                .doOnError((exception) -> log.error("actor skills not found for {}", actorId));
    }

    public Single<ActorSkills> setActorSkills(String actorId, List<Skill> skills) {
        return Single.fromPublisher(
                actorSkillsCollection.findOneAndUpdate(
                        eq("actorId", actorId), set("skills", skills)));
    }


   @PostConstruct
   private void prepareCollections() {
        this.actorSkillsCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getActorSkills(), ActorSkills.class);
    }
}
