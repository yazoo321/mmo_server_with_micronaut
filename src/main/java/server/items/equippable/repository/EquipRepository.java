package server.items.equippable.repository;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.Cacheable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import server.common.configuration.MongoConfiguration;
import server.items.equippable.model.EquippedItems;

@Slf4j
@Singleton
@CacheConfig("actor-equip-cache, actor-equip-cache-map")
public class EquipRepository {

    private static final String ACTOR_EQUIP_CACHE_MAP = "actor-equip-cache-map";
    private static final String ACTOR_EQUIP_CACHE = "actor-equip-cache";

    // This repository is connected to MongoDB
    MongoConfiguration playerCharacterConfiguration;
    MongoClient mongoClient;
    MongoCollection<EquippedItems> equippedItemsCollection;

    public EquipRepository(
            MongoConfiguration playerCharacterConfiguration, MongoClient mongoClient) {
        this.playerCharacterConfiguration = playerCharacterConfiguration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    @CacheInvalidate(
            value = {ACTOR_EQUIP_CACHE_MAP, ACTOR_EQUIP_CACHE},
            parameters = "actorId",
            async = true)
    public Single<EquippedItems> insert(EquippedItems equippedItems, String actorId) {
        return Single.fromPublisher(equippedItemsCollection.insertOne(equippedItems))
                .map(res -> equippedItems);
    }

//    @Cacheable(value = ACTOR_EQUIP_CACHE, parameters = "actorId")
    public Single<List<EquippedItems>> getEquippedItemsForCharacter(String actorId) {
        return Flowable.fromPublisher(equippedItemsCollection.find(eq("actorId", actorId)))
                .toList();
    }

//    @Cacheable(value = ACTOR_EQUIP_CACHE_MAP, parameters = "actorId")
    public Single<Map<String, EquippedItems>> getActorEquippedItems(String actorId) {
        return getEquippedItemsForCharacter(actorId)
                .doOnError(e -> log.error(e.getMessage()))
                .map(
                        items ->
                                items.stream()
                                        .collect(
                                                Collectors.toMap(
                                                        EquippedItems::getCategory,
                                                        Function.identity())));
    }

//    @Cacheable(value = ACTOR_EQUIP_CACHE, parameters = "actorIds")
    public Single<List<EquippedItems>> getEquippedItemsForCharacters(Set<String> actorIds) {
        return Flowable.fromPublisher(equippedItemsCollection.find(in("actorId", actorIds)))
                .toList();
    }

    public Maybe<EquippedItems> getCharacterItemSlot(String actorId, String slotType) {
        return Flowable.fromPublisher(
                        equippedItemsCollection.find(
                                and(eq("actorId", actorId), eq("category", slotType))))
                .firstElement();
    }

    @CacheInvalidate(
            value = {ACTOR_EQUIP_CACHE_MAP, ACTOR_EQUIP_CACHE},
            parameters = "actorId",
            async = true)
    public Single<DeleteResult> deleteEquippedItem(String actorId, String itemInstanceId) {
        // TODO: Consider duplicating item instance ID as nested query is slower
        return Single.fromPublisher(
                equippedItemsCollection.deleteOne(
                        eq("itemInstance.itemInstanceId", itemInstanceId)));
    }

    private void prepareCollections() {
        this.equippedItemsCollection =
                mongoClient
                        .getDatabase(playerCharacterConfiguration.getDatabaseName())
                        .getCollection(
                                playerCharacterConfiguration.getEquipCollection(),
                                EquippedItems.class);
    }
}
