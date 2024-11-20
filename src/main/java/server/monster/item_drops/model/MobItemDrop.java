package server.monster.item_drops.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Serdeable
@NoArgsConstructor
@ReflectiveAccess
public class MobItemDrop {

    public MobItemDrop(
            Integer minLevel, Integer maxLevel, Double baseDropChance, Integer minQuantity, Integer maxQuantity) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.baseDropChance = baseDropChance;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
    }

    Integer minLevel;
    Integer maxLevel;
    Double baseDropChance;
    Integer minQuantity;
    Integer maxQuantity;



}
