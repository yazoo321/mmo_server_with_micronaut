package server.items.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Introspected
@JsonInclude()
@NoArgsConstructor
@Serdeable
public class ItemInstance {


    @BsonCreator
    @JsonCreator
    public ItemInstance(
            @JsonProperty("itemId")
            @BsonProperty("itemId") String itemId,
            @JsonProperty("itemInstanceId")
            @BsonProperty("itemInstanceId") String itemInstanceId,
            @JsonProperty("item")
            @BsonProperty("item") Item item) {

        this.itemId = itemId;
        this.itemInstanceId = itemInstanceId;
        this.item = item;
    }

    String itemId;
    String itemInstanceId;
    Item item;
}
