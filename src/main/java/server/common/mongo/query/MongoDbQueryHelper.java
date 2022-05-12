package server.common.mongo.query;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Flowable;
import org.bson.conversions.Bson;
import server.common.dto.Location;

import java.util.List;

import static com.mongodb.client.model.Filters.and;

public class MongoDbQueryHelper {

    public static <T> List<T> betweenLocation(MongoCollection<T> collection, Location location, Integer threshold) {
        Bson mapEq = Filters.eq("location.map", location.getMap());
        Bson xWithinRange = Filters.and(
                Filters.gt("location.x", (location.getX() - threshold)),
                Filters.lt("location.x", (location.getX() + threshold))
        );
        Bson yWithinRange = Filters.and(
                Filters.gt("location.y", (location.getY() - threshold)),
                Filters.lt("location.y", (location.getY() + threshold))
        );

        return Flowable.fromPublisher(
                collection.find(
                        and(
                                mapEq,
                                and(xWithinRange, yWithinRange)
                        )
                )).toList()
                .blockingGet();
    }
}
