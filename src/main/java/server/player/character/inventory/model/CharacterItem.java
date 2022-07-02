package server.player.character.inventory.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.common.dto.Location2D;
import server.items.model.Item;

@Data
@Introspected
@JsonInclude()
@NoArgsConstructor
public class CharacterItem {

    @BsonCreator
    @JsonCreator
    public CharacterItem(
            @JsonProperty("characterName")
            @BsonProperty("characterName") String characterName,
            @JsonProperty("location")
            @BsonProperty("location") Location2D location,
            @JsonProperty("item")
            @BsonProperty("item") Item item,
            @JsonProperty("itemInstanceId")
            @BsonProperty("itemInstanceId") String itemInstanceId) {

        this.characterName = characterName;
        this.location = location;
        this.itemInstanceId = itemInstanceId;
        this.item = item;
    }
    String characterName;

    // position can be anything you like, 1d, 2d ints, string..
    Location2D location;

    // this is item from ItemInstance - duplicate.
    // this is optimised for faster reads, but will be slower writes and takes more memory
    Item item;

    // for unique identifiers of items
    String itemInstanceId;

}
