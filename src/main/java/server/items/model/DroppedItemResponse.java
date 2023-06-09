package server.items.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DroppedItemResponse {

    List<DroppedItem> droppedItemList;
}
