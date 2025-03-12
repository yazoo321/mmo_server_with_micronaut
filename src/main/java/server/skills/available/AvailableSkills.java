package server.skills.available;

import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Getter;
import server.attribute.stats.model.Stats;
import server.skills.available.cleric.heals.BasicHeal;
import server.skills.available.cleric.heals.HealingRain;
import server.skills.available.fighter.HeavyStrike;
import server.skills.available.fighter.Maim;
import server.skills.available.fighter.Rupture;
import server.skills.available.mage.arcane.Blink;
import server.skills.available.mage.fire.Fireball;
import server.skills.available.mage.nature.EclipseBurst;
import server.skills.available.mage.nature.MoonsVengeance;
import server.skills.available.mage.nature.SunSmite;
import server.skills.available.mage.nature.VineGrab;
import server.skills.model.Skill;

@Getter
@Singleton
public class AvailableSkills {
    // TODO: think of how to remove this class entirely.
    // we already have a Skill factory
    // we can also hold this mapping in a DB/repository

    Map<String, Skill> allSkills = new HashMap<>();

    @PostConstruct
    void populateAllSkills() {
        allSkills.put("fireball", new Fireball());
        allSkills.put("basic heal", new BasicHeal());
        allSkills.put("healing rain", new HealingRain());
        allSkills.put("vine grab", new VineGrab());
        allSkills.put("eclipse burst", new EclipseBurst());
        allSkills.put("moons vengeance", new MoonsVengeance());
        allSkills.put("sun smite", new SunSmite());

        allSkills.put("blink", new Blink());
        allSkills.put("maim", new Maim());
        allSkills.put("rupture", new Rupture());
        allSkills.put("heavy strike", new HeavyStrike());
    }

    public List<Skill> getAvailableSkillsForCharacter(Stats stats) {
        // here we get all character levels from player, and then we can filter out skills that are
        // not available
        // Base stats contain character levels, we check and filter using those

        List<Skill> availableSkills = new ArrayList<>();

        allSkills.forEach(
                (key, value) -> {
                    if (value.isAvailableForCharacter(stats)) {
                        availableSkills.add(value);
                    }
                });

        return availableSkills;
    }

    public Skill getSkillByName(String name) {
        return allSkills.get(name.toLowerCase());
    }
}
