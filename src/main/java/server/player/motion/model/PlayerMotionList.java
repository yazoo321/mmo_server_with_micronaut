package server.player.motion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.player.motion.dto.PlayerMotion;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerMotionList {

    List<PlayerMotion> playerMotionList;
}
