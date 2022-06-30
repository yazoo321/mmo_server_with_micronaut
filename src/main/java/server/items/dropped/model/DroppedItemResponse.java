package server.items.dropped.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DroppedItemResponse {

    List<DroppedItemDto> droppedItemList;
}
