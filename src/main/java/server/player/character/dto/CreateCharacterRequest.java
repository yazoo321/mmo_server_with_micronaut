package server.player.character.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Data
@Introspected
public class CreateCharacterRequest {
    // there will be more as per requirements from UE

    @Pattern(message="Name can only contain letters and numbers", regexp = "^[a-zA-Z0-9]*$")
    @Size(min=3, max=25)
    String name;

    @NotNull
    Map<String, String> appearanceInfo;

    @NotNull
    String className;
}
