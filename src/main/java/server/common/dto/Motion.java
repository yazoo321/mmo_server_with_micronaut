package server.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Introspected
@NoArgsConstructor
@Builder
public class Motion {

    @BsonCreator
    @JsonCreator
    public Motion(
            @JsonProperty("map")
            @BsonProperty("map") String map,

            @JsonProperty("x")
            @BsonProperty("x") Integer x,
            @JsonProperty("y")
            @BsonProperty("y") Integer y,
            @JsonProperty("z")
            @BsonProperty("z") Integer z,

            @JsonProperty("pitch")
            @BsonProperty("pitch") Integer pitch,
            @JsonProperty("roll")
            @BsonProperty("roll") Integer roll,
            @JsonProperty("yaw")
            @BsonProperty("yaw") Integer yaw,

            @JsonProperty("vx")
            @BsonProperty("vx") Integer vx,
            @JsonProperty("vy")
            @BsonProperty("vy") Integer vy,
            @JsonProperty("vz")
            @BsonProperty("vz") Integer vz,

            @JsonProperty("isFalling")
            @BsonProperty("isFalling") Boolean isFalling) {
        this.map = map;

        this.x = x;
        this.y = y;
        this.z = z;

        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;

        this.vy = vy;
        this.vx = vx;
        this.vz = vz;

        this.isFalling = isFalling;
    }

    String map;

    // Position
    Integer x;
    Integer y;
    Integer z;

    // Rotation
    Integer pitch;
    Integer roll;
    Integer yaw;

    // Velocity
    Integer vx;
    Integer vy;
    Integer vz;

    Boolean isFalling;
}
