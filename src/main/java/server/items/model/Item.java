package server.items.model;

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
import server.common.dto.Tag;
import server.items.accessories.Belt;
import server.items.accessories.Cape;
import server.items.accessories.Neck;
import server.items.accessories.Ring;
import server.items.armour.*;
import server.items.consumable.Consumable;
import server.items.weapons.Shield;
import server.items.weapons.Weapon;
import server.player.character.equippable.model.EquippedItems;

import java.util.List;

@Data
@Introspected
@NoArgsConstructor
@BsonDiscriminator
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property= "category")
// define all the serializers that Items can be, weapon, armour, consumable etc.
@JsonSubTypes({
        // Consumables
        @JsonSubTypes.Type(value = Consumable.class, name = "CONSUMABLE"),
        // Weapons
        @JsonSubTypes.Type(value = Weapon.class, name = "WEAPON"),
        @JsonSubTypes.Type(value = Shield.class, name = "SHIELD"),
        // Accessories
        @JsonSubTypes.Type(value = Belt.class, name = "BELT"),
        @JsonSubTypes.Type(value = Cape.class, name = "CAPE"),
        @JsonSubTypes.Type(value = Neck.class, name = "NECK"),
        @JsonSubTypes.Type(value = Ring.class, name = "RING"),
        // Armour
        @JsonSubTypes.Type(value = Boots.class, name = "BOOTS"),
        @JsonSubTypes.Type(value = Bracers.class, name = "BRACERS"),
        @JsonSubTypes.Type(value = Chest.class, name = "CHEST"),
        @JsonSubTypes.Type(value = Gloves.class, name = "GLOVES"),
        @JsonSubTypes.Type(value = Helm.class, name = "HELM"),
        @JsonSubTypes.Type(value = Legs.class, name = "LEGS"),
        @JsonSubTypes.Type(value = Shoulder.class, name = "SHOULDER"),
})
public abstract class Item {

    @BsonCreator
    @JsonCreator
    public Item(
            @JsonProperty("itemId")
            @BsonProperty("itemId") String itemId,
            @JsonProperty("itemName")
            @BsonProperty("itemName") String itemName,
            @JsonProperty("category")
            @BsonProperty("category") String category,
            @JsonProperty("tags")
            @BsonProperty("tags") List<Tag> tags,
            @JsonProperty("stacking")
            @BsonProperty("stacking") Stacking stacking,
            @JsonProperty("value")
            @BsonProperty("value") Integer value,
            @JsonProperty("config")
            @BsonProperty("config") ItemConfig itemConfig) {

        this.itemId = itemId;
        this.itemName = itemName;
        this.category = category;
        this.tags = tags;
        this.stacking = stacking;
        this.value = value;
        this.itemConfig = itemConfig;
    }

    String itemId;
    String itemName;
    String category;
    List<Tag> tags;
    Stacking stacking;
    Integer value;
    ItemConfig itemConfig;

    public abstract EquippedItems createEquippedItem(String characterName, String characterItemId);

}
