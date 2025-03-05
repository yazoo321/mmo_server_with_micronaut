package server.attribute.talents.model;

import io.micronaut.serde.annotation.Serdeable;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Serdeable
@AllArgsConstructor
@NoArgsConstructor
public class ActorTalents {

    private String actorId;
    private Map<String, Integer> learnedTalents;

    public Map<String, Integer> getLearnedTalents() {
        if (learnedTalents == null) {
            learnedTalents = new HashMap<>();
        }

        return learnedTalents;
    }
}
