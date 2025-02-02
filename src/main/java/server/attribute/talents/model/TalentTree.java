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
import server.attribute.talents.available.melee.fighter.weaponmaster.tier1.SharpenedBlades;

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
public class TalentTree {

    protected String name;
    protected String description;
    protected Map<Integer, List<Talent>> tieredTalents;
}
