package server.player.motion.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.common.dto.Motion;

@Data
@JsonInclude()
@Introspected
@NoArgsConstructor
public class PlayerMotion {

    @BsonCreator
    @JsonCreator
    public PlayerMotion(
            @JsonProperty("playerName") @BsonProperty("playerName") String playerName,
            @JsonProperty("motion") @BsonProperty("motion") Motion motion,
            @JsonProperty("isOnline") @BsonProperty("isOnline") Boolean isOnline,
            @JsonProperty("updatedAt") @BsonProperty("updatedAt") Instant updatedAt) {
        this.playerName = playerName;
        this.motion = motion;
        this.isOnline = isOnline;
        this.updatedAt = updatedAt;
    }

    String playerName;
    Motion motion;

    Boolean isOnline;

    Instant updatedAt;
}
