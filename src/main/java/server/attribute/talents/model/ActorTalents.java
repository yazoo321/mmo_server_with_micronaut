package server.attribute.talents.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Serdeable
@AllArgsConstructor
@NoArgsConstructor
public class ActorTalents {

    private String actorId;
    private Map<String, Integer> learnedTalents;
}
