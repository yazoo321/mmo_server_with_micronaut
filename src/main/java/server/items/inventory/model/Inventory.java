package server.items.inventory.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.common.dto.Location2D;

@Data
@Introspected
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
@Serdeable
public class Inventory {

    @BsonCreator
    @JsonCreator
    public Inventory(
            @JsonProperty("characterName") @BsonProperty("characterName") String characterName,
            @JsonProperty("characterItems") @BsonProperty("characterItems")
                    List<CharacterItem> characterItems,
            @JsonProperty("gold") @BsonProperty("gold") Integer gold,
            @JsonProperty("maxSize") @BsonProperty("maxSize") Location2D maxSize) {
        this.characterName = characterName;
        this.characterItems = characterItems;
        this.gold = gold;
        this.maxSize = maxSize;
    }

    String characterName;
    List<CharacterItem> characterItems;
    Integer gold;
    Location2D maxSize;
}
