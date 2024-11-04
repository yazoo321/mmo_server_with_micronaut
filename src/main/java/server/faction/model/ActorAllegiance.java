package server.faction.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Serdeable
@ReflectiveAccess
@NoArgsConstructor
@AllArgsConstructor
public class ActorAllegiance {

    private String actorId;
    private String allegianceName;
}
