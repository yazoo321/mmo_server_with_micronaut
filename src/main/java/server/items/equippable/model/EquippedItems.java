package server.items.equippable.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import server.attribute.stats.types.StatsTypes;
import server.items.equippable.model.types.*;
import server.items.model.ItemInstance;
import server.items.types.ItemType;

@Data
@Introspected
@Serdeable
@NoArgsConstructor
@BsonDiscriminator
@ReflectiveAccess
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "category")
@JsonSubTypes({
    // Weapons
    // Consider renaming weapon to MAINHAND and SHIELD to OFFHAND
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

    public EquippedItems(String actorId, ItemInstance itemInstance, String category) {
        this.actorId = actorId;
        this.itemInstance = itemInstance;
        this.category = category;
    }

    String actorId;
    ItemInstance itemInstance;
    String category;

    public Double getBaseAttackSpeed() {
        if (this.category.equalsIgnoreCase(ItemType.WEAPON.getType())
                || this.category.equalsIgnoreCase(ItemType.SHIELD.getType())) {
            Map<String, Double> effects = this.itemInstance.getItem().getItemEffects();

            return effects.get(StatsTypes.BASE_ATTACK_SPEED.getType());
        }
        return null;
    }

    public Double getAttackDistance() {
        if (this.category.equalsIgnoreCase(ItemType.WEAPON.getType())
                || this.category.equalsIgnoreCase(ItemType.SHIELD.getType())) {
            Map<String, Double> effects = this.itemInstance.getItem().getItemEffects();

            return effects.get(StatsTypes.ATTACK_DISTANCE.getType());
        }
        return null;
    }
}
