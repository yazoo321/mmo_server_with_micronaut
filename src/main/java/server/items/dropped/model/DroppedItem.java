package server.items.dropped.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.common.dto.Location;
import server.items.dto.Item;

import java.time.LocalDateTime;

@Data
@Introspected
@NoArgsConstructor
public class DroppedItem {


    @BsonCreator
    @JsonCreator
    public DroppedItem(
            @JsonProperty("itemId")
            @BsonProperty("itemId") String itemId,
            @JsonProperty("location")
            @BsonProperty("location") Location location,
            @JsonProperty("item")
            @BsonProperty("item") Item item,
            @JsonProperty("updatedAt")
            @BsonProperty("updatedAt") LocalDateTime droppedAt
) {
        // Item ID is duplicate of item -> itemId.
        // reason for duplication is because nested properties are slow to search and we can add index here.
        this.itemId = itemId;
        this.map = map;
        this.location = location;
        this.item = item;
        this.droppedAt = droppedAt;
    }

    String itemId;
    String map;
    Location location;
    Item item;

    // This is to have timeout for items that are dropped/spawned
    LocalDateTime droppedAt;
}
