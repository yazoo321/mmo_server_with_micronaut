package server.player.appearance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Introspected
@NoArgsConstructor
public class AppearancePiece {

    @BsonCreator
    @JsonCreator
    public AppearancePiece(
            @JsonProperty("id")
            @BsonProperty("id") String id,
            @JsonProperty("part")
            @BsonProperty("part") String part,
            @JsonProperty("mesh")
            @BsonProperty("mesh") String mesh,
            @JsonProperty("material")
            @BsonProperty("material") String material,
            @JsonProperty("race")
            @BsonProperty("race") String race,
            @JsonProperty("isMale")
            @BsonProperty("isMale") Boolean isMale,
            @JsonProperty("itemGroup")
            @BsonProperty("itemGroup") String itemGroup,
            @JsonProperty("itemId")
            @BsonProperty("itemId") String itemId
    ) {
        this.id = id;
        this.part = part;
        this.mesh = mesh;
        this.material = material;
        this.race = race;
        this.isMale = isMale;
        this.itemGroup = itemGroup;
        this.itemId = itemId;
    }

    String id;
    String part;
    String mesh;
    String material;
    String race;
    Boolean isMale;
    String itemGroup;
    String itemId;
}