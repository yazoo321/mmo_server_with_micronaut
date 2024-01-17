package server.player.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@Introspected
@JsonInclude()
@AllArgsConstructor
@Serdeable
public class AccountCharactersResponse {
    List<Character> accountCharacters;
}
