package server.motion.socket.v1.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.common.dto.Motion;
import server.motion.dto.PlayerMotion;
import server.motion.model.PlayerMotionList;
import server.motion.repository.PlayerMotionRepository;
import server.motion.service.PlayerMotionService;
import server.util.PlayerMotionUtil;

@MicronautTest
public class PlayerMotionServiceTest {

    @Inject PlayerMotionService playerMotionService;

    @Inject PlayerMotionRepository playerMotionRepository;

    @Inject PlayerMotionUtil playerMotionUtil;

    private static final String TEST_USERNAME = "USER";
    private static final String TEST_CHARACTER_NAME = "CHARACTER";

    @BeforeEach
    void cleanDb() {
        playerMotionUtil.deleteAllPlayerMotionData();
    }

    @Test
    void whenCharacterIsCreatedTheBaseMotionIsAttached() {
        // Given
        Motion base = PlayerMotionService.STARTING_MOTION;
        PlayerMotion playerMotion = new PlayerMotion();
        playerMotion.setMotion(base);
        playerMotion.setIsOnline(false);
        playerMotion.setPlayerName(TEST_CHARACTER_NAME);

        // When
        playerMotionService.initializePlayerMotion(TEST_CHARACTER_NAME).blockingGet();
        PlayerMotion actual =
                playerMotionRepository.findPlayerMotion(TEST_CHARACTER_NAME).blockingGet();

        // Then
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("updatedAt")
                .isEqualTo(playerMotion);
    }

    @Test
    void whenUpdatingMotionForPlayerItsUpdatedAsExpected() {
        // Given
        PlayerMotion playerMotion =
                playerMotionService.initializePlayerMotion(TEST_CHARACTER_NAME).blockingGet();
        Motion motion = playerMotion.getMotion();
        motion.setX(100);
        motion.setY(100);
        motion.setZ(100);
        motion.setVx(200);
        motion.setVy(200);
        motion.setVz(200);
        motion.setRoll(300);
        motion.setYaw(300);
        motion.setPitch(300);

        // updating causes online to be true
        playerMotion.setIsOnline(true);

        // When
        playerMotionService.updatePlayerMotion(TEST_CHARACTER_NAME, motion).blockingGet();
        PlayerMotion actual =
                playerMotionRepository.findPlayerMotion(TEST_CHARACTER_NAME).blockingGet();

        // Then
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("updatedAt")
                .isEqualTo(playerMotion);
        Assertions.assertThat(actual.getUpdatedAt()).isAfter(playerMotion.getUpdatedAt());
    }

    @Test
    void whenUserIsOnlineAndNearbyTheMotionIsReturned() {
        // Given
        PlayerMotion playerMotion =
                playerMotionService.initializePlayerMotion(TEST_CHARACTER_NAME).blockingGet();
        Motion motion = playerMotion.getMotion();
        motion.setX(100);
        motion.setY(100);
        motion.setZ(100);
        playerMotion.setIsOnline(true);

        // update motion and set them online
        playerMotionService.updatePlayerMotion(TEST_CHARACTER_NAME, motion).blockingGet();

        // When
        PlayerMotionList actual =
                playerMotionService.getPlayersNearMe(motion, "FAKE_NAME").blockingGet();

        // Then
        List<PlayerMotion> playerMotions = actual.getPlayerMotionList();
        Assertions.assertThat(playerMotions.size()).isEqualTo(1);
        Assertions.assertThat(playerMotions.get(0))
                .usingRecursiveComparison()
                .ignoringFields("updatedAt")
                .isEqualTo(playerMotion);
    }

    @Test
    void whenUserIsOnlineAndFarTheMotionIsNotReturned() {
        // Given
        PlayerMotion playerMotion =
                playerMotionService.initializePlayerMotion(TEST_CHARACTER_NAME).blockingGet();
        Motion motion = playerMotion.getMotion();
        motion.setX(100);
        motion.setY(100);
        motion.setZ(100);
        playerMotion.setIsOnline(true);

        // update motion and set them online
        playerMotionService.updatePlayerMotion(TEST_CHARACTER_NAME, motion).blockingGet();

        // When
        motion.setX(10000);
        motion.setY(10000);
        PlayerMotionList actual =
                playerMotionService.getPlayersNearMe(motion, "FAKE_NAME").blockingGet();

        // Then
        List<PlayerMotion> playerMotions = actual.getPlayerMotionList();
        Assertions.assertThat(playerMotions.size()).isEqualTo(0);
    }
}
