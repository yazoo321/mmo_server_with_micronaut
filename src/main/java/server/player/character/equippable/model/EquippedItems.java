package server.player.character.equippable.model;

import com.fasterxml.jackson.annotation.*;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.player.character.equippable.model.types.ChestSlot;
import server.player.character.equippable.model.types.HelmSlot;
import server.player.character.equippable.model.types.WeaponSlot1;
import server.player.character.equippable.model.types.ShieldSlot;


@Data
@Introspected
@NoArgsConstructor
@BsonDiscriminator
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property= "category")
@JsonSubTypes({
        @JsonSubTypes.Type(value = WeaponSlot1.class, name = "WEAPON"),
        @JsonSubTypes.Type(value = ShieldSlot.class, name = "SHIELD"),
        @JsonSubTypes.Type(value = HelmSlot.class, name = "HELM"),
        @JsonSubTypes.Type(value = ChestSlot.class, name = "CHEST"),


})
public abstract class EquippedItems {


    @BsonCreator
    @JsonCreator
    public EquippedItems(
            @JsonProperty("characterName")
            @BsonProperty("characterName") String characterName,
            @JsonProperty("characterItemId")
            @BsonProperty("characterItemId") String characterItemId,
            @JsonProperty("category")
            @BsonProperty("category") String category
    ) {
        this.characterName = characterName;
        this.characterItemId = characterItemId;
        this.category = category;
    }

    String characterName;
    String characterItemId;
    String category;
}
