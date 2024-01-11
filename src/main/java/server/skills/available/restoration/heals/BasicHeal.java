package server.skills.available.restoration.heals;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import server.attribute.stats.types.StatsTypes;
import server.skills.active.channelled.ChannelledSkill;

import java.util.Map;


@Getter
@JsonTypeName("Basic heal")
@EqualsAndHashCode(callSuper = false)
public class BasicHeal extends ChannelledSkill {

    public BasicHeal() {
        super(
                "Basic heal",
                "Heal target after a short delay",
                Map.of(
                        StatsTypes.MAGIC_DAMAGE.getType(), -100.0
                ),
                1000,
                1000,
                500,
                Map.of()
        );
    }
}
