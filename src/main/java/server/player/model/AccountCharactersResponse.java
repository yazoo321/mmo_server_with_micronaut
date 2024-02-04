package server.player.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Introspected
@JsonInclude()
@AllArgsConstructor
@Serdeable
public class AccountCharactersResponse {
    List<Character> accountCharacters;
}
