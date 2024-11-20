package server.items.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import server.items.equippable.model.EquippedItems;
import server.items.types.accessories.Belt;
import server.items.types.accessories.Cape;
import server.items.types.accessories.Neck;
import server.items.types.accessories.Ring;
import server.items.types.armour.*;
import server.items.types.consumable.Consumable;
import server.items.types.weapons.Shield;
import server.items.types.weapons.Weapon;

@Data
@Introspected
@NoArgsConstructor
@BsonDiscriminator
@ReflectiveAccess
@Serdeable
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "category")
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

    public Item(
            String itemId,
            String itemName,
            String category,
            Map<String, Double> itemEffects,
            Map<String, Integer> requirements,
            int quality,
            Stacking stacking,
            Integer value,
            ItemConfig itemConfig) {

        this.itemId = itemId;
        this.itemName = itemName;
        this.category = category;
        this.itemEffects = itemEffects;
        this.requirements = requirements;
        this.quality = quality;
        this.stacking = stacking;
        this.value = value;
        this.itemConfig = itemConfig;
    }

    String itemId;
    String itemName;
    String category;
    Map<String, Double> itemEffects;
    Map<String, Integer> requirements;
    Integer quality;
    Stacking stacking;
    Integer value;
    ItemConfig itemConfig;

    @JsonIgnore
    Integer dropChance;

    public abstract EquippedItems createEquippedItem(String actorId, ItemInstance itemInstance);
}
