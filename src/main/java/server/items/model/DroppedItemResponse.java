package server.items.model;

import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Serdeable
public class DroppedItemResponse {

    List<DroppedItem> droppedItemList;
}
