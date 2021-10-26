package server.scheduled;

import io.micronaut.scheduling.annotation.Scheduled;
import server.player.character.repository.PlayerCharacterRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OnlineChecker {

    @Inject
    PlayerCharacterRepository playerCharacterRepository;

    @Scheduled(fixedDelay = "10s")
    void executeEveryTen() {
        playerCharacterRepository.checkAndUpdateUserOnline().blockingGet();
    }
}
