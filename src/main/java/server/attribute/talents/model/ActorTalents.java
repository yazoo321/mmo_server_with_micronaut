package server.attribute.talents.model;

import java.util.Map;
import lombok.Data;

@Data
public class ActorTalents {

    private String actorId;
    private Map<String, Integer> learnedTalents;
}
