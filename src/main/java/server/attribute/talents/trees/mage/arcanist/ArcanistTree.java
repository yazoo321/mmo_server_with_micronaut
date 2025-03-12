package server.attribute.talents.trees.mage.arcanist;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.talents.available.magic.mage.arcanist.tier1.ArcaneInfusion;
import server.attribute.talents.available.magic.mage.arcanist.tier1.ArcaneShielding;
import server.attribute.talents.available.magic.mage.arcanist.tier1.ManaResonance;
import server.attribute.talents.available.magic.mage.arcanist.tier2.ManaSurge;
import server.attribute.talents.available.magic.mage.arcanist.tier2.Spellweaver;
import server.attribute.talents.available.magic.mage.arcanist.tier3.Flicker;
import server.attribute.talents.available.magic.mage.arcanist.tier3.GreaterArcaneShielding;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Serdeable
@JsonTypeName("Arcanist")
@EqualsAndHashCode(callSuper = false)
public class ArcanistTree extends TalentTree {

    public ArcanistTree() {
        this.name = "Arcanist";
        this.description =
                "Specializing in the arcane arts, Arcanists focus on mastering the elements, "
                    + "manipulating the fabric of reality, and harnessing the power of the cosmos.";

        Map<Integer, TalentList> tieredTalents = new HashMap<>();
        Talent arcaneInfusion = new ArcaneInfusion();
        Talent arcaneShielding = new ArcaneShielding();
        Talent manaResonance = new ManaResonance();
        TalentList tier1 = new TalentList(List.of(arcaneInfusion, arcaneShielding, manaResonance));

        Talent manaSurge = new ManaSurge();
        Talent spellweaver = new Spellweaver();
        TalentList tier2 = new TalentList(List.of(manaSurge, spellweaver));

        Talent flicker = new Flicker();
        Talent greaterArcaneShielding = new GreaterArcaneShielding();
        TalentList tier3 = new TalentList(List.of(flicker, greaterArcaneShielding));

        TalentList tier4 = new TalentList(List.of());

        tieredTalents.put(1, tier1);
        tieredTalents.put(2, tier2);
        tieredTalents.put(3, tier3);
        tieredTalents.put(4, tier4);
        this.tieredTalents = tieredTalents;
    }
}
