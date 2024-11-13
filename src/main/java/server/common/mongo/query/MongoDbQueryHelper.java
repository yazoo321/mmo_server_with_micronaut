package server.common.mongo.query;

import static com.mongodb.client.model.Filters.and;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import org.bson.conversions.Bson;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.motion.dto.PlayerMotion;

public class MongoDbQueryHelper {

    public static <T> Single<List<T>> betweenLocation(
            MongoCollection<T> collection, Location location, Integer threshold) {
        Bson mapEq = Filters.eq("location.map", location.getMap());
        Bson xWithinRange =
                Filters.and(
                        Filters.gte("location.x", (location.getX() - threshold)),
                        Filters.lte("location.x", (location.getX() + threshold)));
        Bson yWithinRange =
                Filters.and(
                        Filters.gte("location.y", (location.getY() - threshold)),
                        Filters.lte("location.y", (location.getY() + threshold)));

        return Flowable.fromPublisher(collection.find(and(mapEq, xWithinRange, yWithinRange)))
                .toList();
    }

    public static <T> Single<List<T>> getNearbyPlayers(
            MongoCollection<T> collection, PlayerMotion playerMotion, Integer threshold) {
        Motion motion = playerMotion.getMotion();

        Bson excludingThisCharacter = Filters.ne("actorId", playerMotion.getActorId());
        Bson isOnline = Filters.eq("isOnline", true);
        Bson mapEq = Filters.eq("motion.map", motion.getMap());
        Bson xWithinRange =
                Filters.and(
                        Filters.gte("motion.x", (motion.getX() - threshold)),
                        Filters.lte("motion.x", (motion.getX() + threshold)));
        Bson yWithinRange =
                Filters.and(
                        Filters.gte("motion.y", (motion.getY() - threshold)),
                        Filters.lte("motion.y", (motion.getY() + threshold)));

        // TODO: consider just fetching player names and returning them
        return Flowable.fromPublisher(
                        collection.find(
                                and(
                                        excludingThisCharacter,
                                        isOnline,
                                        mapEq,
                                        xWithinRange,
                                        yWithinRange)))
                .toList();
    }

    public static <T> Single<List<T>> nearbyMobMotionFinder(
            MongoCollection<T> collection, Location location, Integer threshold) {

        Bson mapEq = Filters.eq("motion.map", location.getMap());
        Bson xWithinRange =
                Filters.and(
                        Filters.gte("motion.x", (location.getX() - threshold)),
                        Filters.lte("motion.x", (location.getX() + threshold)));
        Bson yWithinRange =
                Filters.and(
                        Filters.gte("motion.y", (location.getY() - threshold)),
                        Filters.lte("motion.y", (location.getY() + threshold)));

        return Flowable.fromPublisher(collection.find(and(mapEq, xWithinRange, yWithinRange)))
                .toList();
    }
}
