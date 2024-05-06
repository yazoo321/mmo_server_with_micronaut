package server.motion.socket.v1.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.scheduling.TaskExecutors;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Named;
import java.util.concurrent.ExecutorService;
import server.motion.dto.PlayerMotion;
import server.motion.service.PlayerMotionService;

@Controller("/player-motion")
public class PlayerMotionController {

    private final Scheduler scheduler;
    private final PlayerMotionService playerMotionService;

    public PlayerMotionController(
            @Named(TaskExecutors.IO) ExecutorService executorService,
            PlayerMotionService playerMotionService) {
        this.scheduler = Schedulers.from(executorService);
        this.playerMotionService = playerMotionService;
    }

    @Get(value = "/{actorId}")
    Single<PlayerMotion> getPlayerMotion(String actorId) {
        return playerMotionService.getPlayerMotion(actorId);
    }
}
