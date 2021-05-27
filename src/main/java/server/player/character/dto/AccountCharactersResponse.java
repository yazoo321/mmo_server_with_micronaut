package server.player.character.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Introspected
public class AccountCharactersResponse {
    List<Character> accountCharacters;
}
