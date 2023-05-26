package server.items.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import server.items.model.DroppedItem;

import java.util.List;

@Data
@AllArgsConstructor
public class DroppedItemResponse {

    List<DroppedItem> droppedItemList;
}
