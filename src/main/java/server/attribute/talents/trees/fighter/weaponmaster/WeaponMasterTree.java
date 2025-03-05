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
import server.attribute.talents.available.melee.fighter.weaponmaster.tier2.BattleFlow;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier2.CripplingBlows;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier2.PerfectForm;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier3.IronGrip;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier3.SunderingStrikes;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier4.AdrenalineSurge;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier4.ExecutionersPrecision;
import server.attribute.talents.model.Talent;
import server.attribute.talents.model.TalentList;
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

        Map<Integer, TalentList> tieredTalents = new HashMap<>();
        Talent sharpenedBlades = new SharpenedBlades();
        Talent reflexTraining = new ReflexTraining();
        Talent heavyStrikes = new HeavyStrikes();
        TalentList tier1 = new TalentList(List.of(sharpenedBlades, reflexTraining, heavyStrikes));

        Talent cripplingBlows = new CripplingBlows();
        Talent battleFlow = new BattleFlow();
        Talent perfectForm = new PerfectForm();
        TalentList tier2 = new TalentList(List.of(cripplingBlows, battleFlow, perfectForm));

        Talent ironGrip = new IronGrip();
        Talent sunderingStrikes = new SunderingStrikes();
        TalentList tier3 = new TalentList(List.of(ironGrip, sunderingStrikes));

        Talent adrenalineSurge = new AdrenalineSurge();
        Talent executionersPrecision = new ExecutionersPrecision();
        TalentList tier4 = new TalentList(List.of(adrenalineSurge, executionersPrecision));

        tieredTalents.put(1, tier1);
        tieredTalents.put(2, tier2);
        tieredTalents.put(3, tier3);
        tieredTalents.put(4, tier4);
        this.tieredTalents = tieredTalents;
    }
}
