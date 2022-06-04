package server.player.appearance.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Introspected
@NoArgsConstructor
public class MeshMaterialPair {
    @BsonCreator
    @JsonCreator
    public MeshMaterialPair(
            @JsonProperty("mesh")
            @BsonProperty("mesh") String mesh,
            @JsonProperty("material")
            @BsonProperty("material") String material
    ) {

        this.mesh = mesh;
        this.material = material;
    }

    String mesh;
    String material;
}