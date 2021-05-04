package server.player.character.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Introspected
@NoArgsConstructor
public class Character {
    @BsonCreator
    @JsonCreator
    public Character(
            @JsonProperty("name")
            @BsonProperty("name") String name,
            @JsonProperty("xp")
            @BsonProperty("xp") Integer xp,
            @JsonProperty("accountName")
            @BsonProperty("accountName") String accountName) {
        this.name = name;
        this.xp = xp;
        this.accountName = accountName;
    }
    // This DTO should hold all the data that we need to load a player character
    // Hence, this list is far from finished. It will be incremented as development goes on

    // Make sure name only contains letters, allow upper
    @Pattern(message="Name can only contain letters and number", regexp = "^[a-zA-Z0-9]*$")
    @Size(min=3, max=25)
    String name;

    @Min(0)
    Integer xp;

    @NotBlank
    String accountName;
}
