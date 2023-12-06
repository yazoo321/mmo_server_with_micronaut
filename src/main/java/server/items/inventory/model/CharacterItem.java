package server.items.inventory.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location2D;
import server.items.model.ItemInstance;

@Data
@Serdeable
@JsonInclude()
@ReflectiveAccess
@NoArgsConstructor
public class CharacterItem {

    public CharacterItem(String actorId, Location2D location, ItemInstance itemInstance) {

        this.actorId = actorId;
        this.location = location;
        this.itemInstance = itemInstance;
    }

    String actorId;

    // position can be anything you like, 1d, 2d ints, string..
    Location2D location;

    ItemInstance itemInstance;
}
