package server.player.character.inventory.model.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location2D;

import java.util.List;

@Data
@JsonInclude()
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {

    String characterName;
    List<CharacterItemDto> characterItems;
    Integer gold;
    Location2D maxSize;

}
