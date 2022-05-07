package server.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Introspected
@NoArgsConstructor
public class Location {

    @BsonCreator
    @JsonCreator
    public Location(
            @JsonProperty("map")
            @BsonProperty("map") String map,
            @JsonProperty("x")
            @BsonProperty("x") Integer x,
            @JsonProperty("y")
            @BsonProperty("y") Integer y,
            @JsonProperty("z")
            @BsonProperty("z") Integer z
    ) {
        this.map = map;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    String map;
    Integer x;
    Integer y;
    Integer z;

    public boolean matches(Location location) {
        return this.x.equals(location.getX()) && this.y.equals(location.getY()) && this.z.equals(location.getZ());
    }
}
