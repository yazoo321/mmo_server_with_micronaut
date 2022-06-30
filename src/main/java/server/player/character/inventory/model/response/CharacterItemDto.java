package server.player.character.inventory.model.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.common.dto.Location2D;
import server.items.model.Item;
import server.items.model.ItemInstance;

@Data
@JsonInclude()
@NoArgsConstructor
@AllArgsConstructor
public class CharacterItemDto {

    String characterName;
    Location2D location2D;
    Item item;
    ItemInstance itemInstance;

}
