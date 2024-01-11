package server.skills.available.destruction.fire;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import server.attribute.stats.types.StatsTypes;
import server.skills.active.channelled.ChannelledSkill;

import java.util.Map;

@Getter
@JsonTypeName("Basic heal")
@EqualsAndHashCode(callSuper = false)
public class Fireball extends ChannelledSkill {

    public Fireball() {
        super(
                "Fireball",
                "Hurl a fireball at a selected target",
                Map.of(
                        StatsTypes.MAGIC_DAMAGE.getType(), 100.0
                ),
                0,
                1500,
                1000,
                Map.of()
        );
    }
}
