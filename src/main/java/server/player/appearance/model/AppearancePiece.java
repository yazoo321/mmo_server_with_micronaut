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
            @BsonProperty("itemId") String itemId,
            @JsonProperty("isBase")
            @BsonProperty("isBase") Boolean isBase,
            @JsonProperty("isDefault")
            @BsonProperty("isDefault") Boolean isDefault
    ) {
        this.id = id;
        this.part = part;
        this.mesh = mesh;
        this.material = material;
        this.race = race;
        this.isMale = isMale;
        this.itemGroup = itemGroup;
        this.itemId = itemId;
        this.isBase = isBase;
        this.isDefault = isDefault;
    }

    String id;
    String part;        // refers to part of the skeletal mesh appearance acts on, e.g. head, hair, hands, etc
    String mesh;        // refers to skeletal mesh name that we will use to load
    String material;    // refers to material instance name that is applied, several can exist therefore expect multi rows
    String race;        // refers to race applicable for the item, optional if only will exist 1 race
    Boolean isMale;     // refers to gender of item if applicable
    String itemGroup;   // refers to item group/set if required, for instance `cleric_set_01`
    String itemId;      // refers to item id if this appearance piece will be directly related to item
    Boolean isBase;      // refers to whether this is base part, e.g. face, chest, hands (not equip), for evaluating skin opts etc
    Boolean isDefault;  // refers to whether this appearance is default or not, e.g. default opt for hair, skin etc
}