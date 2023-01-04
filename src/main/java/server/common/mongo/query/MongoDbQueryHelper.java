package server.common.mongo.query;

import static com.mongodb.client.model.Filters.and;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import java.util.List;
import org.bson.conversions.Bson;
import server.common.dto.Location;
import server.common.dto.Motion;
import server.player.motion.dto.PlayerMotion;

public class MongoDbQueryHelper {

    public static <T> List<T> betweenLocation(
            MongoCollection<T> collection, Location location, Integer threshold) {
        Bson mapEq = Filters.eq("location.map", location.getMap());
        Bson xWithinRange =
                Filters.and(
                        Filters.gt("location.x", (location.getX() - threshold)),
                        Filters.lt("location.x", (location.getX() + threshold)));
        Bson yWithinRange =
                Filters.and(
                        Filters.gt("location.y", (location.getY() - threshold)),
                        Filters.lt("location.y", (location.getY() + threshold)));

        return Flowable.fromPublisher(collection.find(and(mapEq, and(xWithinRange, yWithinRange))))
                .toList()
                .blockingGet();
    }

    public static <T> List<T> nearbyMotionFinder(
            MongoCollection<T> collection, PlayerMotion playerMotion, Integer threshold) {
        Motion motion = playerMotion.getMotion();

        Bson excludingThisCharacter = Filters.ne("playerName", playerMotion.getPlayerName());
        Bson isOnline = Filters.eq("isOnline", true);
        Bson mapEq = Filters.eq("motion.map", motion.getMap());
        Bson xWithinRange =
                Filters.and(
                        Filters.gt("motion.x", (motion.getX() - threshold)),
                        Filters.lt("motion.x", (motion.getX() + threshold)));
        Bson yWithinRange =
                Filters.and(
                        Filters.gt("motion.y", (motion.getY() - threshold)),
                        Filters.lt("motion.y", (motion.getY() + threshold)));

        return Flowable.fromPublisher(
                        collection.find(
                                and(
                                        excludingThisCharacter,
                                        isOnline,
                                        mapEq,
                                        xWithinRange,
                                        yWithinRange)))
                .toList()
                .blockingGet();
    }
}
