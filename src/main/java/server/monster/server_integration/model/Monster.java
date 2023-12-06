package server.monster.server_integration.model;

import java.time.Instant;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Motion;

@Data
@Serdeable
@NoArgsConstructor
@ReflectiveAccess
public class Monster {

    public Monster(
            String mobId,
            String actorId,
            Motion motion,
            Instant updatedAt,
            String timestamp) {
        this.mobId = mobId;
        this.actorId = actorId;
        this.motion = motion;
        this.updatedAt = updatedAt;
        this.timestamp = timestamp;
    }

    String mobId;
    String actorId;
    Motion motion;
    Instant updatedAt;

    // temp
    String timestamp;
}
