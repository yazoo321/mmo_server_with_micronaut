package server.player.appearance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Map;


@Data
@Introspected
@NoArgsConstructor
public class CharacterAppearance {
    @BsonCreator
    @JsonCreator
    public CharacterAppearance(
            @JsonProperty("characterName")
            @BsonProperty("characterName") String characterName,
            @JsonProperty("race")
            @BsonProperty("race") String race,
            @JsonProperty("skinColor")
            @BsonProperty("skinColor") String skinColor,
            @JsonProperty("isMale")
            @BsonProperty("isMale") Boolean isMale,
            @JsonProperty("properties")
            @BsonProperty("properties") Map<String, MeshMaterialPair> properties
    ) {
        this.characterName = characterName;
        this.race = race;
        this.skinColor = skinColor;
        this.isMale = isMale;
        this.properties = properties;
    }

    String characterName;
    String race;
    String skinColor;
    Boolean isMale;
    Map<String, MeshMaterialPair> properties;
}
