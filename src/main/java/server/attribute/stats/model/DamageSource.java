package server.attribute.stats.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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
