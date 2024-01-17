package server.attribute.status.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import server.items.types.consumable.Consumable;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Data
@Serdeable
@ReflectiveAccess
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "category")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Consumable.class, name = "CONSUMABLE"),
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
