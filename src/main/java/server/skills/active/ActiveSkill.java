package server.skills.active;

import server.common.dto.Location2D;
import server.skills.model.Skill;

import java.util.Map;
import java.util.Set;

public class ActiveSkill extends Skill {

    Integer cooldown;

    public ActiveSkill(String name, String description, Map<String, Double> derived, Integer cooldown, Integer maxRange,
                       Map<String, Integer> requirements) {
        super(name, description, derived, maxRange, requirements);
        this.cooldown = cooldown;
    }
}
