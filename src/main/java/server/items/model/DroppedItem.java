package server.items.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.common.dto.Location;
import server.items.model.ItemInstance;

import java.time.LocalDateTime;

@Data
@Introspected
@JsonInclude()
@NoArgsConstructor
public class DroppedItem {


    @BsonCreator
    @JsonCreator
    public DroppedItem(
            @JsonProperty("droppedItemId")
            @BsonProperty("droppedItemId") String droppedItemId,
            @JsonProperty("location")
            @BsonProperty("location") Location location,
            @JsonProperty("itemInstance")
            @BsonProperty("itemInstance") ItemInstance itemInstance,
            @JsonProperty("updatedAt")
            @BsonProperty("updatedAt") LocalDateTime droppedAt
) {
        this.droppedItemId = droppedItemId;
        this.location = location;
        this.itemInstance = itemInstance;
        this.droppedAt = droppedAt;
    }

    String droppedItemId;
    Location location;
    ItemInstance itemInstance;

    // This is to have timeout for items that are dropped/spawned
    LocalDateTime droppedAt;
}
