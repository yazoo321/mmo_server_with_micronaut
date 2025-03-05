package server.attribute.common.model;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeEffects {
    // should be used by talent and status effects

    private String affectedAttribute;
    private Double additiveModifier;
    private Double multiplyModifier;

    public AttributeEffects(AttributeEffects attributeEffects, int multiplier) {
        this.affectedAttribute = attributeEffects.affectedAttribute;
        this.additiveModifier = attributeEffects.getAdditiveModifier() * multiplier;
        this.multiplyModifier = attributeEffects.getMultiplyModifier() * multiplier;
    }

    public Double getMultiplyModifier() {
        return Objects.requireNonNullElse(multiplyModifier, 0.0);
    }

    public Double getAdditiveModifier() {
        return Objects.requireNonNullElse(additiveModifier, 0.0);
    }
}
