package server.attribute.status.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import server.attribute.status.model.derived.*;

@Data
@Serdeable
@ReflectiveAccess
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "category")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Bleeding.class, name = "BLEEDING"),
    @JsonSubTypes.Type(value = Dead.class, name = "DEAD"),
    @JsonSubTypes.Type(value = Silenced.class, name = "SILENCED"),
    @JsonSubTypes.Type(value = Stunned.class, name = "STUNNED"),
    @JsonSubTypes.Type(value = Unconscious.class, name = "UNCONCIOUS"),
})
public class Status {

    Map<String, Double> derivedEffects;
    Set<String> statusEffects;
    Instant added;
    Instant expiration;
    Boolean canStack;
    String origin;
    String category;
}
