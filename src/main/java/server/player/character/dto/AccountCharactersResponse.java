package server.player.character.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@Introspected
@JsonInclude()
@AllArgsConstructor
public class AccountCharactersResponse {
    List<Character> accountCharacters;
}
