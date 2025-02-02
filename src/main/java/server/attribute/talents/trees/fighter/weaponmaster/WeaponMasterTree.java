package server.attribute.talents.trees.fighter.weaponmaster;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier1.HeavyStrikes;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier1.ReflexTraining;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier1.SharpenedBlades;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier2.CripplingBlows;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentTree;

@Slf4j
@Serdeable
@JsonTypeName("Weaponmaster")
@EqualsAndHashCode(callSuper = false)
public class WeaponMasterTree extends TalentTree {

    public WeaponMasterTree() {
        this.name = "Weaponmaster";
        this.description =
                "Specializing in devastating melee combat, Weaponmasters focus on mastering"
                    + " specific weapons, executing deadly combos, and breaking enemy defenses.";

        Map<Integer, List<Talent>> tieredTalents = new HashMap<>();
        Talent sharpenedBlades = new SharpenedBlades();
        Talent reflexTraining = new ReflexTraining();
        Talent heavyStrikes = new HeavyStrikes();
        List<Talent> tier1 = List.of(sharpenedBlades, reflexTraining, heavyStrikes);

        Talent cripplingBlows = new CripplingBlows();

        List<Talent> tier2 = List.of(cripplingBlows);
        tieredTalents.put(1, tier1);
        tieredTalents.put(2, tier2);
        this.tieredTalents = tieredTalents;
    }
}
