package server.scheduled.user;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import server.motion.repository.PlayerMotionRepository;
import server.player.character.repository.PlayerCharacterRepository;

@Singleton
public class OnlineChecker {

    @Inject PlayerCharacterRepository playerCharacterRepository;

    @Inject PlayerMotionRepository playerMotionRepository;

    //    This is also handled by websocket disconnect function
    @Scheduled(fixedDelay = "30s")
    void executeEveryTen() {
        // TODO: this needs refactoring
        // TODO: this is also a very inefficient call, scope down the time range
//        playerCharacterRepository.checkAndUpdateUserOnline();
        playerMotionRepository.checkAndUpdateUserOnline().subscribe();
    }
}
