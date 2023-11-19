package server.motion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Motion;

@Data
@Serdeable
@JsonInclude()
@ReflectiveAccess
@NoArgsConstructor
public class PlayerMotion {

    public PlayerMotion(
            String playerName,
            Motion motion,
            Boolean isOnline,
            Instant updatedAt) {
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
