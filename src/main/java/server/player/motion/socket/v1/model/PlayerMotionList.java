package server.player.motion.socket.v1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import server.player.motion.dto.PlayerMotion;

import java.util.List;

@Data
@AllArgsConstructor
public class PlayerMotionList {

    List<PlayerMotion> playerMotionList;
}
