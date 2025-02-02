package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.attribute.common.model.AttributeEffects;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.model.Status;
import server.attribute.status.types.StatusTypes;

import java.time.Instant;
import java.util.*;

@Data
@Serdeable
@NoArgsConstructor
@JsonTypeName("MOVE_SLOW")
@EqualsAndHashCode(callSuper = false)
public class MoveMod extends Status {

    public MoveMod(Instant expiration, String sourceId, Double moveSpeedMultiplier) {
        this.setId(UUID.randomUUID().toString());
        this.setAttributeEffects(
                new HashMap<>(
                        Map.of(
                                StatsTypes.MOVE_SPEED.getType(),
                                new AttributeEffects(
                                        StatsTypes.MOVE_SPEED.getType(),
                                        0.0,
                                        moveSpeedMultiplier))));
        this.setStatusEffects(defaultStatusEffects());
        this.setExpiration(expiration);
        this.setCanStack(false);
        this.setOrigin(sourceId);
        this.setCategory(StatusTypes.MOVE_MOD.getType());
    }

    public Set<String> defaultStatusEffects() {
        return new HashSet<>(Set.of(StatusTypes.MOVE_MOD.getType()));
    }
}
