package server.player.motion.socket.v1.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import server.player.motion.dto.PlayerMotion;
import server.player.motion.socket.v1.service.PlayerMotionService;

@Controller("/player-motion")
public class PlayerMotionController {

    @Inject private PlayerMotionService playerMotionService;

    @Get(value = "/{characterName}")
    PlayerMotion getPlayerMotion(String characterName) {
        return playerMotionService.getPlayerMotion(characterName);
    }
}
