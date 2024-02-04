package server.skills.model;

import java.util.List;
import lombok.Data;

@Data
public class ActorSkills {

    private String actorId;
    private List<Skill> skills;
}
