package server.items.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Introspected
@NoArgsConstructor
@Serdeable
public class Stacking {

    @BsonCreator
    @JsonCreator
    public Stacking(
            @JsonProperty("canStack")
            @BsonProperty("canStack") Boolean canStack,
            @JsonProperty("maxStacks")
            @BsonProperty("maxStacks") Integer maxStacks,
            @JsonProperty("stackQuantity")
            @BsonProperty("stackQuantity") Integer stackQuantity) {

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
