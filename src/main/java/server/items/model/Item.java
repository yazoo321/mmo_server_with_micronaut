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
import server.items.armour.Chest;
import server.items.armour.Helm;
import server.items.consumable.Consumable;
import server.items.weapons.Weapon;
import server.player.character.equippable.SlotType;
import server.player.character.equippable.model.EquippableSlots;

import java.util.List;

@Data
@Introspected
@NoArgsConstructor
@BsonDiscriminator
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property= "category")
// define all the serializers that Items can be, weapon, armour, consumable etc.
@JsonSubTypes({
        @JsonSubTypes.Type(value = Weapon.class, name = "WEAPON1H"),
        @JsonSubTypes.Type(value = Chest.class, name = "CHEST"),
        @JsonSubTypes.Type(value = Helm.class, name = "HELM"),
        @JsonSubTypes.Type(value = Consumable.class, name = "CONSUMABLE"),
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

    public abstract List<SlotType> getValidSlotTypes();

    public abstract EquippableSlots createEquippableSlot(String characterName);
}
