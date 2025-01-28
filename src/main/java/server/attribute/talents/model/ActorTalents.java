package server.attribute.talents.model;


import lombok.Data;

import java.util.Map;

@Data
public class ActorTalents {

    private String actorId;
    private Map<String, Integer> learnedTalents;
}
