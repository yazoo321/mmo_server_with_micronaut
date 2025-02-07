package server.motion.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Motion;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Serdeable
public class MotionMessage {

    private Motion motion;
    private Boolean update;
    private String actorId;

    public Boolean getUpdate() {
        if (update == null) {
            update = false;
        }
        return update;
    }
}
