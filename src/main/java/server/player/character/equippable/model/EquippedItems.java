package server.player.character.equippable.model;

import com.fasterxml.jackson.annotation.*;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.items.armour.Boots;
import server.items.armour.Gloves;
import server.player.character.equippable.model.types.*;


@Data
@Introspected
@NoArgsConstructor
@BsonDiscriminator
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property= "category")
@JsonSubTypes({
        // Weapons
        @JsonSubTypes.Type(value = WeaponSlot1.class, name = "WEAPON"),
        @JsonSubTypes.Type(value = ShieldSlot.class, name = "SHIELD"),
        // Accessories
        @JsonSubTypes.Type(value = BeltSlot.class, name = "BELT"),
        @JsonSubTypes.Type(value = CapeSlot.class, name = "CAPE"),
        @JsonSubTypes.Type(value = NeckSlot.class, name = "NECK"),
        @JsonSubTypes.Type(value = RingSlot1.class, name = "RING"),
        // Armour
        @JsonSubTypes.Type(value = BootsSlot.class, name = "BOOTS"),
        @JsonSubTypes.Type(value = BracersSlot.class, name = "BRACERS"),
        @JsonSubTypes.Type(value = ChestSlot.class, name = "CHEST"),
        @JsonSubTypes.Type(value = GlovesSlot.class, name = "GLOVES"),
        @JsonSubTypes.Type(value = HelmSlot.class, name = "HELM"),
        @JsonSubTypes.Type(value = LegsSlot.class, name = "LEGS"),
        @JsonSubTypes.Type(value = ShoulderSlot.class, name = "SHOULDER"),
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
