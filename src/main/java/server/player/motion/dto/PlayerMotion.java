package server.player.motion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import server.common.dto.Motion;

@Data
@AllArgsConstructor
public class PlayerMotion {

    String playerName;
    Motion motion;
}
