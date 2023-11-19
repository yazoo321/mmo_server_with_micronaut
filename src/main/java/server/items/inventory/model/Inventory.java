package server.items.inventory.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location2D;

@Data
@JsonInclude()
@ReflectiveAccess
@NoArgsConstructor
@Serdeable
public class Inventory {

    public Inventory(
            String characterName,
            List<CharacterItem> characterItems,
            Integer gold,
            Location2D maxSize) {
        this.characterName = characterName;
        this.characterItems = characterItems;
        this.gold = gold;
        this.maxSize = maxSize;
    }

    String characterName;
    List<CharacterItem> characterItems;
    Integer gold;
    Location2D maxSize;
}
