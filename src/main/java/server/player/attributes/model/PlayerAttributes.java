package server.player.attributes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Builder
@Introspected
@NoArgsConstructor
public class PlayerAttributes {

    @BsonCreator
    @JsonCreator
    public PlayerAttributes(
            @JsonProperty("playerName") @BsonProperty("playerName") String playerName,
            @JsonProperty("baseAttributes") @BsonProperty("baseAttributes")
                    Map<String, Integer> baseAttributes,
            @JsonProperty("attributesAdded") @BsonProperty("attributesAdded")
                    Map<String, Integer> attributesAdded,
            @JsonProperty("currentAttributes") @BsonProperty("currentAttributes")
                    Map<String, Integer> currentAttributes) {
        this.playerName = playerName;
        this.baseAttributes = baseAttributes;
        this.attributesAdded = attributesAdded;
        this.currentAttributes = currentAttributes;
    }

    String playerName;
    Map<String, Integer> baseAttributes;
    Map<String, Integer> attributesAdded;
    Map<String, Integer> currentAttributes;
}
