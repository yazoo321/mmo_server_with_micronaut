package server.items.inventory.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
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
            String actorId, List<CharacterItem> characterItems, Integer gold, Location2D maxSize) {
        this.actorId = actorId;
        this.characterItems = characterItems;
        this.gold = gold;
        this.maxSize = maxSize;
    }

    String actorId;
    List<CharacterItem> characterItems;
    Integer gold;
    Location2D maxSize;
}
