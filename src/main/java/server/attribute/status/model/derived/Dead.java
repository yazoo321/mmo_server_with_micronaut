package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import server.attribute.status.model.Status;
import server.attribute.status.types.StatusTypes;

@Data
@Serdeable
@JsonTypeName("DEAD")
@EqualsAndHashCode(callSuper = false)
public class Dead extends Status {

    public Dead() {
        this.setId(UUID.randomUUID().toString());
        this.setAttributeEffects(Map.of());
        this.setStatusEffects(defaultStatusEffects());
        this.setExpiration(null);
        this.setMaxStacks(1);
        this.setSourceActor(null);
        this.setCategory(StatusTypes.DEAD.getType());
    }

    public Set<String> defaultStatusEffects() {
        return new HashSet<>(
                Set.of(
                        StatusTypes.DEAD.getType(),
                        StatusTypes.CANNOT_ACT.getType(),
                        StatusTypes.CANNOT_MOVE.getType(),
                        StatusTypes.CANNOT_ATTACK.getType(),
                        StatusTypes.CANNOT_CAST.getType(),
                        StatusTypes.CANNOT_HEAL.getType()));
    }
}
