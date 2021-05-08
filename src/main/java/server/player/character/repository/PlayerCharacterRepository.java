package server.player.character.repository;

import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.subscribers.DefaultSubscriber;
import server.configuration.PlayerCharacterConfiguration;
import server.player.character.dto.Character;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.validation.Valid;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@Singleton
public class PlayerCharacterRepository {
    // This repository is connected to MongoDB
    PlayerCharacterConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Character> characters;

    public PlayerCharacterRepository(
            PlayerCharacterConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        this.characters = getCollection();
    }

    @PostConstruct
    public void createIndex() {
        // Micronaut does not yet support index annotation, we have to create manually
        // https://www.javaer101.com/en/article/20717814.html

        characters.createIndex(Indexes.text("name"))
                .subscribe(new DefaultSubscriber<>() {
                    @Override
                    public void onNext(String s) {
                        System.out.format("Index %s was created.%n", s);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Completed");
                    }
                });
    }

    public Single<Character> save(@Valid Character character) {
        return findByName(character.getName())
                .switchIfEmpty(
                        Single.fromPublisher(
                                characters.insertOne(character))
                                .map(success -> character)
                );
    }

    public Single<Character> createNew(@Valid Character character) {
        // detect if we find character
        boolean exists = findByName(character.getName()).blockingGet() != null;

        if (exists) {
            // change to another error
            // this way we can keep the interface of .blockingGet and avoid nullptr ex
            return Single.error(new NullPointerException());
        }

        return save(character);
    }

    public Maybe<Character> findByName(String name) {
        // TODO: Ignore case
        return Flowable.fromPublisher(
                characters
                        .find(eq("name", name))
                        .limit(1)
        ).firstElement();
    }

    public Single<List<Character>> findByAccount(String accountName) {
        // TODO: Ignore case
        return Flowable.fromPublisher(
                characters.find(eq("accountName", accountName))
        ).toList();
    }

    public Single<DeleteResult> deleteByCharacterName(String name) {
        return Single.fromPublisher(
                characters.deleteOne(eq("name", name))
        );
    }

    private MongoCollection<Character> getCollection() {
        return mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), Character.class);
    }
}
