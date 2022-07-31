package server.player.motion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Motion;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMotion {

    String playerName;
    Motion motion;

    Boolean isOnline;
    ZonedDateTime updatedAt;
}
