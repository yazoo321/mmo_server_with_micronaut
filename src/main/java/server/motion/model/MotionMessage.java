package server.motion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Motion;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MotionMessage {
// sample message:
//        {
//        "update": true,
//        "motion":{
//            "x":37293,
//            "y":-65466,
//            "z":-20639
//        }
//        "mobInstanceId:"some_instance_id_1"
//     }

    private Motion motion;
    private Boolean update;
    private String mobInstanceId;

    public Boolean getUpdate() {
        if (update == null) {
            update = false;
        }
        return update;
    }
}
