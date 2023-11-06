package server.items.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.common.dto.Location;

@Data
@Introspected
@JsonInclude()
@NoArgsConstructor
@Serdeable
public class DroppedItem {

    @BsonCreator
    @JsonCreator
    public DroppedItem(
            @JsonProperty("itemInstanceId") @BsonProperty("itemInstanceId") String itemInstanceId,
            @JsonProperty("location") @BsonProperty("location") Location location,
            @JsonProperty("itemInstance") @BsonProperty("itemInstance") ItemInstance itemInstance,
            @JsonProperty("updatedAt") @BsonProperty("updatedAt") LocalDateTime droppedAt) {
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
    LocalDateTime droppedAt;
}
