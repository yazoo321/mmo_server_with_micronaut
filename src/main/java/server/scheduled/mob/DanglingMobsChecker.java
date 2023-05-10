package server.scheduled.mob;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.monster.server_integration.repository.MobRepository;

import java.time.Instant;

@Slf4j
@Singleton
public class DanglingMobsChecker {
    // UE server can go down and not be in control of the mobs
    // in this case, we want to delete the mobs

    @Inject
    MobRepository mobRepository;

    private static final Integer TIME_THRESHOLD_SECONDS = 20;

    @Scheduled(fixedDelay = "60s")
    void executeEveryTen() {
        mobRepository.deleteMobsNotUpdatedWithin(Instant.now().minusSeconds(TIME_THRESHOLD_SECONDS))
                .doOnError(error -> log.error("Failed to clear dangling mobs, {}", error.getMessage()))
                .subscribe();

    }
}
