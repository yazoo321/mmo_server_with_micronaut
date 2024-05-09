package server.items.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location;

@Data
@JsonInclude()
@NoArgsConstructor
@ReflectiveAccess
@Serdeable
public class DroppedItem {

    public DroppedItem(
            String itemInstanceId,
            Location location,
            ItemInstance itemInstance,
            Instant droppedAt) {
        this.itemInstanceId = itemInstanceId;
        this.location = location;
        this.itemInstance = itemInstance;
        this.droppedAt = droppedAt;
    }

    // denormalized for faster search
    String itemInstanceId;
    Location location;
    ItemInstance itemInstance;

    // This is to have timeout for items that are dropped/spawned
    Instant droppedAt;
}
