package server.skills.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActorSkillsRef {

    private String actorId;
    private List<String> skills; // store the skill name only in repository

    public ActorSkillsRef(ActorSkills actorSkills) {
        this.actorId = actorSkills.getActorId();
        this.skills = actorSkills.getSkills().stream().map(Skill::getName).collect(Collectors.toList());
    }
}
