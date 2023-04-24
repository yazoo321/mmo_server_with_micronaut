package server.monster.server_integration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Motion;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MobMotionMessage {
    // sample message:
    //    {
    //    "update": true,
    //    "motion":{
    //        "x":37293,
    //        "y":-65466,
    //        "z":-20639
    //    }
    //}

    private Motion motion;
    private Boolean update;

}
