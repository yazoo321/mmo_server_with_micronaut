package server.player.controller;

import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import server.player.motion.dto.PlayerMotion;
import server.player.motion.dto.PlayerMotionList;
import server.player.motion.service.PlayerMotionService;

import javax.inject.Inject;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/player")
public class PlayerController {

    @Inject
    PlayerMotionService playerService;

    @Post("/update-motion")
    public PlayerMotionList updatePlayerLocation(@Body PlayerMotion playerMotionRequest) {
        playerService.updatePlayerState(playerMotionRequest);
        return playerService.getPlayersNearMe(playerMotionRequest.getPlayerName());
    }
}