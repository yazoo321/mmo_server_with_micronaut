package server.player.controller;

import io.micronaut.http.annotation.*;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import server.common.dto.Motion;
import server.player.model.AccountCharactersResponse;
import server.player.model.Character;
import server.player.model.CreateCharacterRequest;
import server.player.service.PlayerCharacterService;

// @Secured(SecurityRule.IS_AUTHENTICATED)
@Slf4j
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
        log.info("create character call: {}", createCharacterRequest);
        return playerCharacterService.createCharacter(createCharacterRequest, accountName);
    }

    @Get("/characters")
    public AccountCharactersResponse getCharacters(@QueryValue List<String> names) {

        return playerCharacterService.getCharacters(names);
    }

    @Delete(value = "/{actorId}")
    public void deleteCharacter(@Header String accountName, String actorId) {
        // TODO: validation that character belongs to account
        playerCharacterService.deleteCharacter(actorId);
    }

    @Patch(value = "/respawn-character/{actorId}")
    public Single<Motion> respawnCharacter(@Header String accountName, String actorId) {
        // TODO: validate request
        return playerCharacterService.respawnPlayer(actorId);
    }

    @Post("/respawn-character")
    public Character respawnCharacter(
            @Body CreateCharacterRequest createCharacterRequest,
            @Header String accountName) {
        return playerCharacterService.createCharacter(createCharacterRequest, accountName);
    }

}
