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
public class Location2D {

    @BsonCreator
    @JsonCreator
    public Location2D(
            @JsonProperty("x")
            @BsonProperty("x") Integer x,
            @JsonProperty("y")
            @BsonProperty("y") Integer y
    ) {
        this.x = x;
        this.y = y;
    }

    Integer x;
    Integer y;

    public boolean matches(Location2D location) {
        return this.x.equals(location.getX()) && this.y.equals(location.getY());
    }
}
