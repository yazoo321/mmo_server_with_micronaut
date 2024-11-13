package server.attribute.status.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import server.attribute.status.helpers.StatusTestHelper;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Dead;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatusServiceTest {


    @Inject
    StatusTestHelper statusTestHelper;

    @Inject StatusService statusService;

    @BeforeEach
    void reset() {
        statusTestHelper.resetStatuses(List.of("actor1"));
    }


    // Test case class for parameterized testing
    static class TestCase {
        String actorId;
        ActorStatus initialStatus;
        ActorStatus expectedStatus;

        public TestCase(String actorId, ActorStatus initialStatus, ActorStatus expectedStatus) {
            this.actorId = actorId;
            this.initialStatus = initialStatus;
            this.expectedStatus = expectedStatus;
        }
    }
    static Stream<TestCase> inputs() {
        ActorStatus initialStatus = new ActorStatus("actor1", new HashSet<>(), false, Set.of());

        return Stream.of(
                new TestCase("actor1", initialStatus, initialStatus)
        );
    }

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("Test addStatusToActor")
    void testAddStatusToActor(TestCase testCase) {
        statusService.addStatusToActor(testCase.initialStatus, Set.of(new Dead()));

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(2, ChronoUnit.SECONDS))
                .until(() -> {
                    ActorStatus actorStatus = statusService.getActorStatus(testCase.actorId).blockingGet();

                    Set<String> statusCategories = actorStatus.getActorStatuses()
                            .stream().map(Status::getCategory).collect(Collectors.toSet());

                    return statusCategories.contains("DEAD");
                });
    }
}
