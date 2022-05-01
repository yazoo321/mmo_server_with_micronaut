package server.items.repository;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Flowable;
import server.configuration.PlayerCharacterConfiguration;
import server.items.dto.Item;

import javax.inject.Singleton;

import static com.mongodb.client.model.Filters.eq;

@Singleton
public class ItemRepository {

    // This repository is connected to MongoDB
    PlayerCharacterConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Item> item;

    public ItemRepository(
            PlayerCharacterConfiguration configuration,
            MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public Item findByItemId(String itemId) {
        return Flowable.fromPublisher(
                item
                        .find(eq("itemId", itemId))
                        .limit(1)
        ).firstElement().blockingGet();
    }


    private void prepareCollections() {
        this.item = mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), Item.class);
    }
}
