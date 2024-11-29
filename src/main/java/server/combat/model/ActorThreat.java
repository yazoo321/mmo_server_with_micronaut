package server.combat.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Serdeable
@NoArgsConstructor
@AllArgsConstructor
public class ActorThreat {

    String actorId;
    Map<String, Integer> actorThreat;

}
