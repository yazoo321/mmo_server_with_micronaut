package server.items.armour;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.common.dto.Tag;
import server.items.model.Item;
import server.items.model.ItemConfig;
import server.items.model.Stacking;
import server.items.types.ItemType;

import java.util.List;

@Data
@NoArgsConstructor
@JsonTypeName("armour")
@EqualsAndHashCode(callSuper=false)
public class Armour extends Item {

    public Armour(String itemId, String itemName, List<Tag> tags, Stacking stacking, Integer value, ItemConfig config) {
        super(itemId, itemName, ItemType.ARMOUR.getType(), tags, stacking, value, config);
    }

    // use tags to get specific dynamic data

}
