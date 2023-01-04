package server.player.controller;

import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import java.util.List;
import javax.validation.Valid;
import server.player.character.dto.AccountCharactersResponse;
import server.player.character.dto.Character;
import server.player.character.dto.CreateCharacterRequest;
import server.player.character.service.PlayerCharacterService;

// @Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/player")
public class PlayerController {

    @Inject PlayerCharacterService playerCharacterService;

    @Get("/account-characters")
    public AccountCharactersResponse getAccountCharacters(@Header String accountName) {
        // This endpoint will be for when user logs in.
        // They will be greeted with a list of characters
        return playerCharacterService.getAccountCharacters(accountName);
    }

    @Post("/create-character")
    public Character createCharacter(
            @Body @Valid CreateCharacterRequest createCharacterRequest,
            @Header String accountName) {

        return playerCharacterService.createCharacter(createCharacterRequest, accountName);
    }

    @Get("/characters")
    public AccountCharactersResponse getCharacters(@QueryValue List<String> names) {

        return playerCharacterService.getCharacters(names);
    }
}
