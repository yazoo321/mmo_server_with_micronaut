package server.player.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import java.time.Instant;
import java.util.Map;
import javax.validation.constraints.*;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Introspected
@NoArgsConstructor
@Serdeable
public class Character {
    // TODO: Break this DTO into multiple classes

    @BsonCreator
    @JsonCreator
    public Character(
            @JsonProperty("name") @BsonProperty("name") String name,
            @JsonProperty("accountName") @BsonProperty("accountName") String accountName,
            @JsonProperty("appearanceInfo") @BsonProperty("appearanceInfo")
                    Map<String, String> appearanceInfo,
            @JsonProperty("updatedAt") @BsonProperty("updatedAt") Instant updatedAt,
            @JsonProperty("isOnline") @BsonProperty("isOnline") Boolean isOnline) {
        this.name = name;
        this.accountName = accountName;
        this.appearanceInfo = appearanceInfo;
        this.updatedAt = updatedAt;
        this.isOnline = isOnline;
    }

    // Make sure name only contains letters, allow upper
    @Pattern(message = "Name can only contain letters and number", regexp = "^[a-zA-Z0-9]*$")
    @Size(min = 3, max = 25)
    String name;

    @NotBlank String accountName;

    @NotNull Map<String, String> appearanceInfo;

    Instant updatedAt;

    Boolean isOnline;
}
