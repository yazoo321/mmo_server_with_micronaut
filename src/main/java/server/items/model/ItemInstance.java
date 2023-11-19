package server.items.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude()
@NoArgsConstructor
@ReflectiveAccess
@Serdeable
public class ItemInstance {

    public ItemInstance(
            String itemId,
            String itemInstanceId,
            Item item) {

        this.itemId = itemId;
        this.itemInstanceId = itemInstanceId;
        this.item = item;
    }

    String itemId;
    String itemInstanceId;
    Item item;
}
