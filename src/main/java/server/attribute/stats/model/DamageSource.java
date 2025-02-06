package server.attribute.stats.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DamageSource {

    private String actorId;
    private String sourceActorId;
    private String sourceSkillId;
    private String sourceStatusId;
    private Map<String, Double> damageMap;
    private String additionalData;
}
