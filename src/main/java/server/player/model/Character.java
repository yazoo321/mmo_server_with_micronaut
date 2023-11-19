package server.player.model;

import java.time.Instant;
import java.util.Map;
import javax.validation.constraints.*;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Serdeable
@NoArgsConstructor
@ReflectiveAccess
public class Character {
    // TODO: Break this DTO into multiple classes

    public Character(
            String name,
            String accountName,
            Map<String, String> appearanceInfo,
            Instant updatedAt,
            Boolean isOnline) {
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
