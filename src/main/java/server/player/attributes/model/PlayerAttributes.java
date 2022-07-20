package server.player.attributes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.common.dto.NumTag;
import server.common.dto.Tag;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Introspected
@NoArgsConstructor
public class PlayerAttributes {

    @BsonCreator
    @JsonCreator
    public PlayerAttributes(
            @JsonProperty("playerName")
            @BsonProperty("playerName") String playerName,
            @JsonProperty("baseAttributes")
            @BsonProperty("baseAttributes") List<NumTag> baseAttributes,
            @JsonProperty("attributesAdded")
            @BsonProperty("attributesAdded") List<NumTag> attributesAdded,
            @JsonProperty("currentAttributes")
            @BsonProperty("currentAttributes") List<NumTag> currentAttributes
    ) {
        this.playerName = playerName;
        this.baseAttributes = baseAttributes;
        this.attributesAdded = attributesAdded;
        this.currentAttributes = currentAttributes;
    }

    String playerName;
    List<NumTag> baseAttributes;
    List<NumTag> attributesAdded;
    List<NumTag> currentAttributes;

}
