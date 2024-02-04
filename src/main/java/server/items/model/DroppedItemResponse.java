package server.items.model;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Serdeable
public class DroppedItemResponse {

    List<DroppedItem> droppedItemList;
}
