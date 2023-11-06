package server.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import java.time.Instant;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Introspected
@NoArgsConstructor
@Builder
@Serdeable
public class ActorAction {

    @BsonCreator
    @JsonCreator
    public ActorAction(
            @JsonProperty("actionId") @BsonProperty("actionId") String actionId,
            @JsonProperty("state") @BsonProperty("state") String state,
            @JsonProperty("target") @BsonProperty("target") String target,
            @JsonProperty("location") @BsonProperty("location") Location location,
            @JsonProperty("startedAt") @BsonProperty("startedAt") Instant startedAt) {

        this.actionId = actionId;
        this.state = state;
        this.target = target;
        this.location = location;
        this.startedAt = startedAt;
    }

    String actionId;
    String state;
    String target;
    Location location;
    Instant startedAt;
}
