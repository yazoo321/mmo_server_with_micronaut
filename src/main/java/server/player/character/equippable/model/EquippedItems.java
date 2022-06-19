package server.player.character.equippable.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Map;

@Data
@Introspected
@JsonInclude()
@NoArgsConstructor
public class EquippedItems {

    @BsonCreator
    @JsonCreator
    public EquippedItems(
            @JsonProperty("characterName")
            @BsonProperty("characterName") String characterName,
            @JsonProperty("equippedSlots")
            @BsonProperty("equippedSlots") Map<String, EquippableSlots> equippedSlots
    ) {

        this.characterName = characterName;
        this.equippedSlots = equippedSlots;
    }

    String characterName;
    Map<String, EquippableSlots> equippedSlots;
}
