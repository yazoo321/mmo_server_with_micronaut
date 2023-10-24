package server.combat.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CombatRequest {

    private Set<String> targets;
    private Location location;

    private String skillId;

    private String itemInstanceId;
}
