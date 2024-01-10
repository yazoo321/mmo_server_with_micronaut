package server.skills.active.channelled;

import server.skills.active.ActiveSkill;

import java.util.Map;

public abstract class ChannelledSkill extends ActiveSkill {

    int castTime;

    public ChannelledSkill(String name, String description, Map<String, Double> derived, int cooldown, int castTime) {
        super(name, description, derived, cooldown);
        this.castTime = castTime;
    }

}
