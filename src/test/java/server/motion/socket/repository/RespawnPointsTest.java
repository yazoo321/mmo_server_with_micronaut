package server.motion.socket.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import server.common.dto.Location;
import server.motion.repository.RespawnPoints;

import java.util.stream.Stream;

public class RespawnPointsTest {

    private RespawnPoints respawnPoints;

    @BeforeEach
    public void setUp() {
        respawnPoints = new RespawnPoints();
    }

    // Create a method source for parameterized tests
    static Stream<TestCase> inputs() {
        Location tooksworthLocation = new Location("Tooksworth", 240, 350, 230);

        return Stream.of(
                new TestCase("Tooksworth", "town", tooksworthLocation, tooksworthLocation),
                new TestCase("Tooksworth", "checkpoint", tooksworthLocation, tooksworthLocation),
                new TestCase("Tooksworth", "nearest", new Location("Tooksworth", 5, 5, 5), tooksworthLocation)
        );
    }

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("Test RespawnPoints with different types")
    public void testGetRespawnPointFor(TestCase testCase) {
        // Act
        Location result = respawnPoints.getRespawnPointFor(testCase.map, testCase.type, testCase.point);

        // Assert
        Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(testCase.expectedLocation);
    }

    // Test case class to store input and expected output for parameterized tests
    static class TestCase {
        String map;
        String type;
        Location point;
        Location expectedLocation;

        public TestCase(String map, String type, Location point, Location expectedLocation) {
            this.map = map;
            this.type = type;
            this.point = point;
            this.expectedLocation = expectedLocation;
        }
    }
}