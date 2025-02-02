package server.attribute.talents.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
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
