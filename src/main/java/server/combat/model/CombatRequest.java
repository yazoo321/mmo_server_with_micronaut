package server.combat.model;

import java.util.Set;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Serdeable
@ReflectiveAccess
public class CombatRequest {

    private Set<String> targets;
    private Location location;

    private String skillId;

    private String itemInstanceId;
}
