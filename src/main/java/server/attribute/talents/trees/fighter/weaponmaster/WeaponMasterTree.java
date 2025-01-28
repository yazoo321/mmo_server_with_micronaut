package server.attribute.talents.trees.fighter.weaponmaster;

import server.attribute.talents.available.melee.fighter.weaponmaster.tier1.SharpenedBlades;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeaponMasterTree extends TalentTree {

    public WeaponMasterTree() {
        this.name = "Weaponmaster";
        this.description = "Specializing in devastating melee combat, Weaponmasters focus on mastering specific weapons, " +
                "executing deadly combos, and breaking enemy defenses.";

        Map<Integer, List<Talent>> tieredTalents = new HashMap<>();
        Talent sharpenedBlades = new SharpenedBlades();
        List<Talent> tier1 = List.of(sharpenedBlades);

        tieredTalents.put(1, tier1);
        this.tieredTalents = tieredTalents;
    }
}
