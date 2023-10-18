package server.combat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CombatRequest {

    private Set<String> targets;
    private Location location;

    private String skillId;

    private String itemInstanceId;

}
