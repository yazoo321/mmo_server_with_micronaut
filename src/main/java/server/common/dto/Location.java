package server.common.dto;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Serdeable
@ReflectiveAccess
public class Location {

    public Location(String map, Integer x, Integer y, Integer z) {
        this.map = map;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    String map;
    Integer x;
    Integer y;
    Integer z;

    public boolean matches(Location location) {
        return this.x.equals(location.getX())
                && this.y.equals(location.getY())
                && this.z.equals(location.getZ());
    }

    public Location(Motion motion) {
        this.map = motion.getMap();
        this.x = motion.getX();
        this.y = motion.getY();
        this.z = motion.getZ();
    }

    public boolean withinThreshold(Location l2, int threshold) {
        if (l2 == null || l2.getX() == null || l2.getY() == null || l2.getMap() == null) {
            return false;
        }

        if (!this.getMap().equalsIgnoreCase(l2.getMap())) {
            return false;
        }

        int x = l2.getX();
        int y = l2.getY();

        if (this.getX() < (x - threshold) || this.getX() > (x + threshold)) {
            return false;
        }

        return this.getY() >= (y - threshold) && this.getY() <= (y + threshold);

        // can add Z if we want to.
    }
}
