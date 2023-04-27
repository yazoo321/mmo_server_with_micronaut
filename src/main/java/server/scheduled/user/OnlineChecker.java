package server.scheduled.user;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import server.player.character.repository.PlayerCharacterRepository;
import server.motion.repository.PlayerMotionRepository;

@Singleton
public class OnlineChecker {

    @Inject PlayerCharacterRepository playerCharacterRepository;

    @Inject PlayerMotionRepository playerMotionRepository;

//    This is now handled via websockets connect/disconnect
//    @Scheduled(fixedDelay = "10s")
    void executeEveryTen() {
        // TODO: this needs refactoring
        // TODO: this is also a very inefficient call, scope down the time range
        playerCharacterRepository.checkAndUpdateUserOnline();
        playerMotionRepository.checkAndUpdateUserOnline();
    }
}
