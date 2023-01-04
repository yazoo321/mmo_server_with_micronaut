package server.scheduled.user;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import server.player.character.repository.PlayerCharacterRepository;
import server.player.motion.socket.v1.repository.PlayerMotionRepository;
import jakarta.inject.Inject;

@Singleton
public class OnlineChecker {

    @Inject
    PlayerCharacterRepository playerCharacterRepository;

    @Inject
    PlayerMotionRepository playerMotionRepository;

    @Scheduled(fixedDelay = "10s")
    void executeEveryTen() {
        // TODO: this needs refactoring
        // TODO: this is also a very inefficient call, scope down the time range
        playerCharacterRepository.checkAndUpdateUserOnline();
        playerMotionRepository.checkAndUpdateUserOnline();
    }
}
