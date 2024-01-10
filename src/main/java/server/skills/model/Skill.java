package server.skills.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "name")
public abstract class Skill {

    private String name;
    private String description;
    Map<String, Double> derived;


//    public abstract void applySkill();
}
