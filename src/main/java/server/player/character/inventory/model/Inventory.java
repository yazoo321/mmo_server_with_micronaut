package server.player.character.inventory.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

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
            @BsonProperty("characterItems") List<CharacterItem> characterItems
            ) {
        this.characterName = characterName;
        this.characterItems = characterItems;
    }

    String characterName;
    List<CharacterItem> characterItems;
}
