package server.attribute.talents.model;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

import lombok.Data;
import server.attribute.common.model.AttributeEffects;

@Data
@Serdeable
public class ActorTalentAttributeEffects {

    private String actorId;
    private Map<String, AttributeEffects> attributeEffects;
}
