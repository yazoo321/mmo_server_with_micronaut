package server.player.attributes.helpers;

import static com.mongodb.client.model.Filters.ne;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import server.configuration.MongoConfiguration;
import server.player.attributes.model.PlayerAttributes;

@Singleton
public class PlayerAttributeTestHelper {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<PlayerAttributes> attributesCollection;

    public PlayerAttributeTestHelper(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public void deleteAllAttributeData() {
        Single.fromPublisher(attributesCollection.deleteMany(ne("playerName", "deleteAll")))
                .blockingGet();
    }

    private void prepareCollections() {
        this.attributesCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getPlayerAttributes(), PlayerAttributes.class);
    }
}
