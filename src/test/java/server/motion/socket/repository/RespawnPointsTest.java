package server.motion.socket.repository;

import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import server.common.dto.Location;
import server.motion.repository.RespawnPoints;

public class RespawnPointsTest {

    private RespawnPoints respawnPoints;

    @BeforeEach
    public void setUp() {
        respawnPoints = new RespawnPoints();
    }

    // Create a method source for parameterized tests
    static Stream<TestCase> inputs() {
        Location tooksworthLocation = new Location("tooksworth", 240, 350, 230);

        return Stream.of(
                new TestCase("tooksworth", "town", tooksworthLocation, tooksworthLocation),
                new TestCase("tooksworth", "checkpoint", tooksworthLocation, tooksworthLocation),
                new TestCase(
                        "tooksworth",
                        "nearest",
                        new Location("tooksworth", 5, 5, 5),
                        tooksworthLocation));
    }

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("Test RespawnPoints with different types")
    public void testGetRespawnPointFor(TestCase testCase) {
        // Act
        Location result =
                respawnPoints.getRespawnPointFor(testCase.map, testCase.type, testCase.point);

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
