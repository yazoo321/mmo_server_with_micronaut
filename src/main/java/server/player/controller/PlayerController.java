package server.player.controller;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import server.account.dto.Account;
import server.player.character.dto.AccountCharactersResponse;
import server.player.character.dto.Character;
import server.player.character.dto.CreateCharacterRequest;
import server.player.character.service.PlayerCharacterService;
import server.player.motion.dto.PlayerMotion;
import server.player.motion.dto.PlayerMotionList;
import server.player.motion.service.PlayerMotionService;

import javax.inject.Inject;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/player")
public class PlayerController {

    @Inject
    PlayerMotionService playerMotionService;

    @Inject
    PlayerCharacterService playerCharacterService;

    @Post("/update-motion")
    public PlayerMotionList updatePlayerLocation(@Body PlayerMotion playerMotionRequest) {
        playerMotionService.updatePlayerState(playerMotionRequest);
        return playerMotionService.getPlayersNearMe(playerMotionRequest.getPlayerName());
    }

    @Get("/account-characters")
    public AccountCharactersResponse getAccountCharacters(Principal principal) {
        // This endpoint will be for when user logs in.
        // They will be greeted with a list of characters
        return playerCharacterService.getAccountCharacters(principal.getName());
    }

    @Post("/create-character")
    public Character createCharacter(@Body @Valid CreateCharacterRequest createCharacterRequest, Principal principal) {
        // Principal is the authenticated user, we should not get it from body but JWT token as that is trusted
        String accountName = principal.getName();

        return playerCharacterService.createCharacter(createCharacterRequest, accountName);
    }
}