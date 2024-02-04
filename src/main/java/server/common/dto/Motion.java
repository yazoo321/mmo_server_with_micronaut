package server.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.time.temporal.ValueRange;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Builder
@Serdeable
@JsonInclude
@ReflectiveAccess
@NoArgsConstructor
public class Motion {

    public Motion(
            String map,
            Integer x,
            Integer y,
            Integer z,
            Integer pitch,
            Integer roll,
            Integer yaw,
            Integer vx,
            Integer vy,
            Integer vz,
            Boolean isFalling) {
        this.map = map;

        this.x = x;
        this.y = y;
        this.z = z;

        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;

        this.vy = vy;
        this.vx = vx;
        this.vz = vz;

        this.isFalling = isFalling;
    }

    String map;

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

    Boolean isFalling;

    public boolean withinRange(Motion motion, int threshold) {
        boolean xValid = validDimension(this.getX(), motion.getX(), threshold);
        boolean yValid = validDimension(this.getY(), motion.getY(), threshold);
        boolean zValid = validDimension(this.getZ(), motion.getZ(), threshold);

        return xValid && yValid && zValid;
    }

    private boolean validDimension(long v1, long v2, int threshold) {
        return ValueRange.of(v1 - threshold, v1 + threshold).isValidValue(v2);
    }

    public boolean facingMotion(Motion motion2) {
        double maxAngle = 90;

        // Calculate direction vectors for the player and the monster
        double x1 = this.getX();
        double y1 = this.getY();

        double x2 = motion2.getX();
        double y2 = motion2.getY();

        double vectorX = x2 - x1;
        double vectorY = y2 - y1;

        // Calculate direction vector based on yaw (horizontal angle)
        double directionX = Math.cos(Math.toRadians(yaw));
        double directionY = Math.sin(Math.toRadians(yaw));

        double dotProduct = directionX * vectorX + directionY * vectorY;

        double magnitudeDirection = Math.sqrt(directionX * directionX + directionY * directionY);
        double magnitudeVector = Math.sqrt(vectorX * vectorX + vectorY * vectorY);

        double cosTheta = dotProduct / (magnitudeDirection * magnitudeVector);
        double angleRad = Math.acos(cosTheta);

        double degrees = Math.toDegrees(angleRad);

        return degrees < maxAngle;
    }
}
