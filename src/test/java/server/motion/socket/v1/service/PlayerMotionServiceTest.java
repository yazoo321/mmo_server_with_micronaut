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
import server.motion.repository.ActorMotionRepository;
import server.motion.repository.PlayerMotionRepository;
import server.motion.service.PlayerMotionService;
import server.util.PlayerMotionUtil;

@MicronautTest
public class PlayerMotionServiceTest {

    @Inject PlayerMotionService playerMotionService;

    @Inject PlayerMotionRepository playerMotionRepository;

    @Inject PlayerMotionUtil playerMotionUtil;

    private static final String TEST_USERNAME = "USER";
    private static final String TEST_ACTOR_ID = "CHARACTER";

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
        playerMotion.setActorId(TEST_ACTOR_ID);

        // When
        playerMotionService.initializePlayerMotion(TEST_ACTOR_ID).blockingGet();
        PlayerMotion actual = playerMotionRepository.fetchPlayerMotion(TEST_ACTOR_ID).blockingGet();

        // Then
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("updatedAt")
                .isEqualTo(playerMotion);
    }
}
