package server.attribute.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class AttributeEffects {
    // should be used by talent and status effects

    private String affectedAttribute;
    private Double additiveModifier;
    private Double multiplyModifier;

    public Double getMultiplyModifier() {
        return Objects.requireNonNullElse(multiplyModifier, 1.0);
    }

}
