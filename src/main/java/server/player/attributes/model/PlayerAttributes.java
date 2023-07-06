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
@Deprecated // use Stats instead
public class PlayerAttributes {

    @BsonCreator
    @JsonCreator
    public PlayerAttributes(
            @JsonProperty("playerName") @BsonProperty("playerName") String playerName,
            @JsonProperty("baseAttributes") @BsonProperty("baseAttributes")
                    Map<String, Integer> baseAttributes,
            @JsonProperty("currentAttributes") @BsonProperty("currentAttributes")
                    Map<String, Integer> currentAttributes,
            @JsonProperty("points") @BsonProperty("points")
                    Integer attributePoints
    ) {
        this.playerName = playerName;
        this.baseAttributes = baseAttributes;
        this.currentAttributes = currentAttributes;
        this.attributePoints = attributePoints;
    }

    String playerName;
    Map<String, Integer> baseAttributes;
    Map<String, Integer> currentAttributes;
    Integer attributePoints;
}
