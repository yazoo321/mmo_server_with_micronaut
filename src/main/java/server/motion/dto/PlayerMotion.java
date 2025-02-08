package server.motion.dto;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Motion;

import java.time.Instant;

@Data
@Serdeable
@ReflectiveAccess
@NoArgsConstructor
public class PlayerMotion {

    public PlayerMotion(String actorId, Motion motion, Boolean isOnline, Instant updatedAt) {
        this.actorId = actorId;
        this.motion = motion;
        this.isOnline = isOnline;
        this.updatedAt = updatedAt;
    }

    String actorId;
    Motion motion;
    Boolean isOnline;
    Instant updatedAt;
}
