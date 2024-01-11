package server.skills.active.channelled;

import server.skills.active.ActiveSkill;

import java.util.Map;

public abstract class ChannelledSkill extends ActiveSkill {

    int castTime;

    public ChannelledSkill(String name, String description, Map<String, Double> derived, int cooldown, int castTime,
                           int maxRange, Map<String, Integer> requirements) {
        super(name, description, derived, cooldown, maxRange, requirements);
        this.castTime = castTime;
    }

}
