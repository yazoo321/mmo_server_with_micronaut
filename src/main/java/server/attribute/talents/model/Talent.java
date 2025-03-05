package server.attribute.talents.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import server.attribute.common.model.AttributeEffects;
import server.attribute.common.model.AttributeRequirements;
import server.attribute.stats.model.Stats;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier1.SharpenedBlades;
import server.attribute.talents.service.TalentService;

@Slf4j
@Serdeable
@Getter
@ReflectiveAccess
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "name")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SharpenedBlades.class, name = "Sharpened blades"),
})
// TODO: We probably don't need reflective access
public abstract class Talent {

    protected String name;
    protected String description;
    protected String talentType;
    protected List<AttributeEffects> attributeEffects;
    protected String applyType;
    protected Integer levels;
    protected String treeName;
    protected Integer tier;
    protected AttributeRequirements attributeRequirements;

    // this applies based on applyType, potentially will require renaming
    public void applyEffect(
            Integer level, TalentService talentService, Stats actorStats, Stats targetStats) {
        return;
    }

    public int isAvailableForCharacter(
            Stats stats, Map<String, Integer> learnedTalents, Map<String, Integer> pointsPerTree) {
        Map<String, Integer> base = stats.getBaseStats();

        // checks base stats to make sure level requirements are met
        boolean baseRequirements =
                this.attributeRequirements.getRequirements().entrySet().stream()
                        .allMatch(
                                entry -> base.getOrDefault(entry.getKey(), 0) >= entry.getValue());

        if (!baseRequirements) {
            return 0;
        }

        int pointsRequired = this.getTier() * 5 - 5;

        if (pointsPerTree.getOrDefault(this.getTreeName(), 0) < pointsRequired) {
            return 0;
        }

        // the dependencies refer to talent pre-requisites that may exist
        // we check them against actors existing learned talents

        // example; current level = 1; dependency is level 1; we can level to 2;
        int talentLevel = learnedTalents.getOrDefault(this.getName(), 0);
        talentLevel++; // if we have talent level 0, we _want_ to level it to 1. if its level 1,
        // then 2, etc.

        // set high min level if there are no requirements
        // otherwise, we can only match up to the dependency level
        int minDependencyLevel =
                attributeRequirements.getDependencies().isEmpty()
                        ? 10
                        : attributeRequirements.getDependencies().stream()
                                .map(dependency -> learnedTalents.getOrDefault(dependency, 0))
                                .min(Integer::compare)
                                .orElse(0);

        int ableToLevel = Math.min(talentLevel, minDependencyLevel);

        if (ableToLevel > this.getLevels()) {
            // should not be larger
            return 0;
        }

        return ableToLevel;
    }
}

//    public Talent(String name,
//                  String description,
//                  List<AttributeEffects> attributeEffects,
//                  AttributeRequirements attributeRequirements,
//                  Integer levels,
//                  String treeName,
//                  Integer tier,
//                  String talentType) {
//        this.name = name;
//        this.description = description;
//        this.attributeEffects = attributeEffects;
//        this.attributeRequirements = attributeRequirements;
//        this.levels = levels;
//        this.treeName = treeName;
//        this.tier = tier;
//        this.talentType = talentType;
//    }
