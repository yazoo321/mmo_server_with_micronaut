package server.player.character.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import server.common.dto.Motion;

import javax.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Introspected
@NoArgsConstructor
public class Character {
    // TODO: Break this DTO into multiple classes

    @BsonCreator
    @JsonCreator
    public Character(
            @JsonProperty("name")
            @BsonProperty("name") String name,
            @JsonProperty("accountName")
            @BsonProperty("accountName") String accountName,
            @JsonProperty("appearanceInfo")
            @BsonProperty("appearanceInfo") Map<String, String> appearanceInfo,
            @JsonProperty("updatedAt")
            @BsonProperty("updatedAt") Instant updatedAt,
            @JsonProperty("isOnline")
            @BsonProperty("isOnline") Boolean isOnline
            ) {
        this.name = name;
        this.accountName = accountName;
        this.appearanceInfo = appearanceInfo;
        this.updatedAt = updatedAt;
        this.isOnline = isOnline;
    }

    // Make sure name only contains letters, allow upper
    @Pattern(message="Name can only contain letters and number", regexp = "^[a-zA-Z0-9]*$")
    @Size(min=3, max=25)
    String name;

    @NotBlank
    String accountName;

    @NotNull
    Map<String, String> appearanceInfo;

    Instant updatedAt;

    Boolean isOnline;
}
