package server.player.character.inventory.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.items.dto.Item;

@Data
@Introspected
@NoArgsConstructor
public class CharacterItem {

    @BsonCreator
    @JsonCreator
    public CharacterItem(
            @JsonProperty("characterName")
            @BsonProperty("characterName") String characterName,
            @JsonProperty("item")
            @BsonProperty("item") Item item,
            @JsonProperty("position")
            @BsonProperty("position") String position) {
        this.characterName = characterName;
        this.item = item;
        this.position = position;
    }
    String characterName;
    Item item;
    String position;
}
