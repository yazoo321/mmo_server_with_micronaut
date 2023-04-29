package server.scheduled.user;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import server.motion.repository.PlayerMotionRepository;
import server.player.character.repository.PlayerCharacterRepository;

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
