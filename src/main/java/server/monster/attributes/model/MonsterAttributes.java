package server.monster.attributes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Map;

@Data
@Builder
@Introspected
@NoArgsConstructor
public class MonsterAttributes {

    @BsonCreator
    @JsonCreator
    public MonsterAttributes(
            @JsonProperty("mobId") @BsonProperty("mobId") String mobId,
            @JsonProperty("mobInstanceId") @BsonProperty("mobInstanceId") String mobInstanceId,
            @JsonProperty("baseAttributes") @BsonProperty("baseAttributes")
                    Map<String, Integer> baseAttributes,
            @JsonProperty("currentAttributes") @BsonProperty("currentAttributes")
                    Map<String, Integer> currentAttributes) {
        this.mobId = mobId;
        this.mobInstanceId = mobInstanceId;
        this.baseAttributes = baseAttributes;
        this.currentAttributes = currentAttributes;
    }

    String mobId;
    String mobInstanceId;
    Map<String, Integer> baseAttributes;
    Map<String, Integer> currentAttributes;
}
