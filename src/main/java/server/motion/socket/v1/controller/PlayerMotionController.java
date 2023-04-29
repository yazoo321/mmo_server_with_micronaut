package server.motion.socket.v1.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import server.motion.dto.PlayerMotion;
import server.motion.service.PlayerMotionService;

@Deprecated
@Controller("/player-motion")
public class PlayerMotionController {

    @Inject private PlayerMotionService playerMotionService;

    @Get(value = "/{characterName}")
    Single<PlayerMotion> getPlayerMotion(String characterName) {
        return playerMotionService.getPlayerMotion(characterName);
    }
}
