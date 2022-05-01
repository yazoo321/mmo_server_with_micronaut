package server.items.dropped.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.items.dto.Item;

import java.time.LocalDateTime;

@Data
@Introspected
@NoArgsConstructor
public class DroppedItem {


    @BsonCreator
    @JsonCreator
    DroppedItem(
            @JsonProperty("x")
            @BsonProperty("x") Integer x,
            @JsonProperty("y")
            @BsonProperty("y") Integer y,
            @JsonProperty("z")
            @BsonProperty("z") Integer z,
            @JsonProperty("item")
            @BsonProperty("item") Item item,
            @JsonProperty("updatedAt")
            @BsonProperty("updatedAt") LocalDateTime droppedAt
) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.item = item;
        this.droppedAt = droppedAt;
    }

    Integer x;
    Integer y;
    Integer z;

    Item item;

    // This is to have timeout for items that are dropped/spawned
    LocalDateTime droppedAt;
}
