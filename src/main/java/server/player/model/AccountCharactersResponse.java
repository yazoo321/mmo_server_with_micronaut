package server.player.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.Introspected;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Introspected
@JsonInclude()
@AllArgsConstructor
public class AccountCharactersResponse {
    List<Character> accountCharacters;
}
