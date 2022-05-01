package server.items.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.common.dto.Tag;

import java.util.List;

@Data
@Introspected
@NoArgsConstructor
public class Item {

    @BsonCreator
    @JsonCreator
    public Item(
            @JsonProperty("itemId")
            @BsonProperty("itemId") String itemId,
            @JsonProperty("itemName")
            @BsonProperty("itemName") String itemName,
            @JsonProperty("category")
            @BsonProperty("category") String category,
            @JsonProperty("tags")
            @BsonProperty("tags") List<Tag> tags) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.category = category;
        this.tags = tags;
    }

    String itemId;
    String itemName;
    String category;
    List<Tag> tags;

}
