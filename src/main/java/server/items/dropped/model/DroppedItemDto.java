package server.items.dropped.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location;
import server.items.model.Item;
import server.items.model.ItemInstance;

import java.time.LocalDateTime;

@Data
@JsonInclude
@NoArgsConstructor
@AllArgsConstructor
public class DroppedItemDto {

    String droppedItemId;
    Location location;
    String itemInstanceId;
    Item item;
    LocalDateTime droppedAt;


    public DroppedItemDto(DroppedItem droppedItem, Item item, ItemInstance instance) {
        this.droppedItemId = droppedItem.getDroppedItemId();
        this.location = droppedItem.getLocation();
        this.itemInstanceId = droppedItem.getItemInstanceId();

        // merge item instance content with item content
        item.getTags().addAll(instance.getTags());

        this.item = item;
        this.droppedAt = droppedItem.getDroppedAt();
    }
}
