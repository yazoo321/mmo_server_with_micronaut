package server.player.character.inventory.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.common.dto.Location2D;

import java.util.List;

@Data
@Introspected
@NoArgsConstructor
public class Inventory {

    @BsonCreator
    @JsonCreator
    public Inventory(
            @JsonProperty("characterName")
            @BsonProperty("characterName") String characterName,
            @JsonProperty("characterItems")
            @BsonProperty("characterItems") List<CharacterItem> characterItems,
            @JsonProperty("gold")
            @BsonProperty("gold") Integer gold,
            @JsonProperty("maxSize")
            @BsonProperty("maxSize") Location2D maxSize
            ) {
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
