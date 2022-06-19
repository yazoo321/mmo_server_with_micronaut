package server.player.character.equippable.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.player.character.equippable.model.types.ChestSlot;
import server.player.character.equippable.model.types.HelmSlot;
import server.player.character.equippable.model.types.WeaponSlot1;
import server.player.character.equippable.model.types.WeaponSlot2;

@Data
@Introspected
@NoArgsConstructor
@BsonDiscriminator
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property= "category")
// Ensure this matches with SlotType class
@JsonSubTypes({
        @JsonSubTypes.Type(value = WeaponSlot1.class, name = "WEAPON1"),
        @JsonSubTypes.Type(value = WeaponSlot2.class, name = "WEAPON2"),
        @JsonSubTypes.Type(value = ChestSlot.class, name = "CHEST"),
        @JsonSubTypes.Type(value = HelmSlot.class, name = "HELM"),
})
public abstract class EquippableSlots {

    @BsonCreator
    @JsonCreator
    public EquippableSlots(
            @JsonProperty("characterName")
            @BsonProperty("characterName") String characterName,
            @JsonProperty("category")
            @BsonProperty("category") String category) {

        this.characterName = characterName;
        this.category = category;
    }

    String characterName;
    String category;
}


