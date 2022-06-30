package server.items.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude()
@NoArgsConstructor
public class ItemInstance {


    @BsonCreator
    @JsonCreator
    public ItemInstance(
            @JsonProperty("itemId")
            @BsonProperty("itemId") String itemId,
            @JsonProperty("itemInstanceId")
            @BsonProperty("itemInstanceId") String itemInstanceId,
            @JsonProperty("tags")
            @BsonProperty("tags") List<Tag> tags) {

        this.itemId = itemId;
        this.itemInstanceId = itemInstanceId;
        this.tags = tags;
    }

    String itemId;
    String itemInstanceId;
    List<Tag> tags;
}
