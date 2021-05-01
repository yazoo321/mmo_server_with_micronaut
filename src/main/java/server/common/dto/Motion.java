package server.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Motion {

    // Position
    Integer x;
    Integer y;
    Integer z;

    // Rotation
    Integer pitch;
    Integer roll;
    Integer yaw;

    // Velocity
    Integer vx;
    Integer vy;
    Integer vz;
}
