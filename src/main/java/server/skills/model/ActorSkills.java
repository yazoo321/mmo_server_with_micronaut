package server.skills.model;

import lombok.Data;

import java.util.List;

@Data
public class ActorSkills {

    private String actorId;
    private List<Skill> skills;

}
