package server.common.dto;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@Builder
@Serdeable
@ReflectiveAccess
public class ActorAction {

    public ActorAction(
            String actionId,
            String state,
            String target,
            Location location,
            Instant startedAt) {

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
