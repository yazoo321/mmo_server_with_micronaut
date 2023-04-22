package server.monster.server_integration.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.common.dto.Motion;

@Data
@Introspected
@NoArgsConstructor
public class MobMotion {

    @BsonCreator
    @JsonCreator
    public MobMotion(
            @JsonProperty("mobId") @BsonProperty("mobId") String mobId,
            @JsonProperty("mobInstanceId") @BsonProperty("mobInstanceId") String mobInstanceId,
            @JsonProperty("motion") @BsonProperty("motion") Motion motion,
            @JsonProperty("updatedAt") @BsonProperty("updatedAt") Instant updatedAt) {
        this.mobId = mobId;
        this.mobInstanceId = mobInstanceId;
        this.motion = motion;
        this.updatedAt = updatedAt;
    }

    String mobId;
    String mobInstanceId;
    Motion motion;
    Instant updatedAt;
}
