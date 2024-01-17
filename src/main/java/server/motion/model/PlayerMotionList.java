package server.motion.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.motion.dto.PlayerMotion;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude
@AllArgsConstructor
@NoArgsConstructor
@Serdeable
public class PlayerMotionList {

    List<PlayerMotion> playerMotionList;

    public List<PlayerMotion> getPlayerMotionList() {
        if (playerMotionList == null) {
            playerMotionList = new ArrayList<>();
        }
        return playerMotionList;
    }
}
