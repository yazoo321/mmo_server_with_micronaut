package server.items.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Introspected
@NoArgsConstructor
@Serdeable
public class ItemConfig {

    @BsonCreator
    @JsonCreator
    public ItemConfig(
            @JsonProperty("icon")
            @BsonProperty("icon") String icon,
            @JsonProperty("mesh")
            @BsonProperty("mesh") String mesh) {

        this.icon = icon;
        this.mesh = mesh;
    }

    String icon;
    String mesh;
}
