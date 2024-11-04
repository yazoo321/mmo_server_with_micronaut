package server.faction.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Serdeable
@ReflectiveAccess
@NoArgsConstructor
@AllArgsConstructor
public class HostileAllegiance {

    private String allegianceName;       // The primary allegiance
    private String hostileTo;            // The target allegiance it's hostile towards
    private int hostilityLevel;          // Hostility level between allegiances
    // assume 0 is very hostile, 5 is neutral and 10 is very friendly

}
