package server.player.motion.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.player.motion.dto.PlayerMotion;

@Data
@JsonInclude
@AllArgsConstructor
@NoArgsConstructor
public class PlayerMotionList {

    List<PlayerMotion> playerMotionList;

    public List<PlayerMotion> getPlayerMotionList() {
        if (playerMotionList == null) {
            playerMotionList = new ArrayList<>();
        }
        return playerMotionList;
    }
}
