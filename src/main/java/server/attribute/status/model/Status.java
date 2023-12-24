package server.attribute.status.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import server.items.types.accessories.Belt;
import server.items.types.accessories.Cape;
import server.items.types.accessories.Neck;
import server.items.types.accessories.Ring;
import server.items.types.armour.*;
import server.items.types.consumable.Consumable;
import server.items.types.weapons.Shield;
import server.items.types.weapons.Weapon;

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
