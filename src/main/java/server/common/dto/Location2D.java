package server.common.dto;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Serdeable
@ReflectiveAccess
public class Location2D {

    public Location2D(
            Integer x,
            Integer y
    ) {
        this.x = x;
        this.y = y;
    }

    Integer x;
    Integer y;

    public boolean matches(Location2D location) {
        return this.x.equals(location.getX()) && this.y.equals(location.getY());
    }
}
