package server.combat.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.skills.model.SkillTarget;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Serdeable
@ReflectiveAccess
public class CombatRequest {

    private Set<String> targets;

    private String skillId;
    private SkillTarget skillTarget;
    private Boolean channelSuccess;

    private String itemInstanceId;

    private String actorId;
}
