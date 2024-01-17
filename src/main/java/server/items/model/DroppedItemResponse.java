package server.items.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Serdeable
public class DroppedItemResponse {

    List<DroppedItem> droppedItemList;
}
