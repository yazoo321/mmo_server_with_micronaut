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
import server.items.model.Item;
import server.items.model.ItemInstance;
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
            @JsonProperty("itemInstanceId")
            @BsonProperty("itemInstanceId") String itemInstanceId,
            @JsonProperty("item")
            @BsonProperty("item") Item item,
            @JsonProperty("category")
            @BsonProperty("category") String category
    ) {
        this.characterName = characterName;
        this.itemInstanceId = itemInstanceId;
        this.item = item;
        this.category = category;
    }

    String characterName;
    String itemInstanceId;
    Item item;
    String category;

    public EquippedItems(String characterName, ItemInstance itemInstance, String category) {
        this.characterName = characterName;
        this.itemInstanceId = itemInstance.getItemInstanceId();
        this.item = itemInstance.getItem();
        this.category = category;
    }
}
