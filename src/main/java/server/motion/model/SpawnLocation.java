package server.motion.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Objects;


@NoArgsConstructor
@AllArgsConstructor
public class SpawnLocation {

    String map;
    String type;

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof SpawnLocation other))
            return false;

        return Objects.equals(this.map, other.map) && Objects.equals(this.type, other.type);
    }

    @Override
    public final int hashCode() {
        int result = 17;
        if (map != null) {
            result = 31 * result + map.hashCode();
        }
        if (type != null) {
            result = 31 * result + type.hashCode();
        }
        return result;
    }

}
