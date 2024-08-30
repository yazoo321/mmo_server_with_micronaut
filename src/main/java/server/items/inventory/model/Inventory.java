package server.items.inventory.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.Optional;

import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location2D;
import server.items.inventory.model.exceptions.InventoryException;

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

    public CharacterItem getItemAtLocation(Location2D location) {
        Optional<CharacterItem> item =
                characterItems.stream().filter(i -> i.getLocation().equals(location)).findFirst();

        return item.orElse(null);
    }

    public CharacterItem getItemByInstanceId(String instanceId) {
        Optional<CharacterItem> item =
                characterItems.stream().filter(i -> i.getItemInstance().getItemInstanceId().equals(instanceId)).findFirst();

        return item.orElse(null);
    }

    public Location2D getNextAvailableSlot() {
        int[][] invArr = new int[maxSize.getX()][maxSize.getY()];

        characterItems.forEach(
                i -> {
                    Location2D loc = i.getLocation();
                    // process only valid locations, ignore 'equipped' items
                    if (loc != null && loc.getX() > -1) {
                        invArr[loc.getX()][loc.getY()] = 1;
                    }
                });

        for (int x = 0; x < maxSize.getY(); x++) {
            for (int y = 0; y < maxSize.getX(); y++) {
                if (invArr[x][y] != 1) {
                    return new Location2D(x, y);
                }
            }
        }

        throw new InventoryException("No available slots in inventory");
    }
}
