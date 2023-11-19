package server.items.model;


import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ReflectiveAccess
@Serdeable
public class Stacking {

    public Stacking(
            Boolean canStack,
            Integer maxStacks,
            Integer stackQuantity) {

        this.canStack = canStack;
        this.stackQuantity = stackQuantity;
        this.maxStacks = maxStacks;
    }

    Integer maxStacks;
    Boolean canStack;
    Integer stackQuantity;

    public Boolean getCanStack() {
        // avoid NPE
        return Boolean.TRUE.equals(canStack);
    }

    public Integer getMaxStacks() {
        if (!this.canStack) {
            return 1;
        } else {
            return maxStacks;
        }
    }
}
