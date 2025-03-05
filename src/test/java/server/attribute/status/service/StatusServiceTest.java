package server.attribute.status.service;

import static org.awaitility.Awaitility.await;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.helpers.StatusTestHelper;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Burning;
import server.attribute.status.model.derived.Dead;
import server.attribute.talents.model.ActorTalents;
import server.attribute.talents.repository.TalentRepository;
import server.session.SessionParamHelper;
import server.socket.session.FakeSession;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatusServiceTest {

    @Inject SessionParamHelper sessionParamHelper;

    @MockBean(SessionParamHelper.class)
    public SessionParamHelper sessionParamHelper() {
        return Mockito.mock(SessionParamHelper.class);
    }

    @Mock FakeSession session;

    @Inject StatusTestHelper statusTestHelper;

    @Inject StatsService statsService;

    @Inject StatusService statusService;

    @Inject TalentRepository talentRepository;

    @BeforeAll
    void reset() {
        configureRun();
    }

    @BeforeEach
    void resetEach() {
        configureRun();
    }

    private void configureRun() {
        statusTestHelper.resetStatuses(List.of(TEST_ACTOR));
        statsService.deleteStatsFor(TEST_ACTOR).blockingSubscribe();
        statsService.initializePlayerStats(TEST_ACTOR).blockingSubscribe();
        talentRepository.insertActorTalents(
                TEST_ACTOR, new ActorTalents(TEST_ACTOR, new HashMap<>()));

        statusTestHelper.resetStatuses(List.of(TEST_ACTOR_2));
        statsService.deleteStatsFor(TEST_ACTOR_2).blockingSubscribe();
        statsService.initializePlayerStats(TEST_ACTOR_2).blockingSubscribe();
        talentRepository.insertActorTalents(
                TEST_ACTOR_2, new ActorTalents(TEST_ACTOR_2, new HashMap<>()));

        MockitoAnnotations.openMocks(this);
    }

    private static final String TEST_ACTOR = "actor1";
    private static final String TEST_ACTOR_2 = "actor2";

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

        return Stream.of(new TestCase("actor1", initialStatus, initialStatus));
    }

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("Test addStatusToActor")
    void testAddStatusToActor(TestCase testCase) {
        statusService.addStatusToActor(testCase.initialStatus, Set.of(new Dead()));

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(2, ChronoUnit.SECONDS))
                .until(
                        () -> {
                            ActorStatus actorStatus =
                                    statusService.getActorStatus(testCase.actorId).blockingGet();

                            Set<String> statusCategories =
                                    actorStatus.getActorStatuses().stream()
                                            .map(Status::getCategory)
                                            .collect(Collectors.toSet());

                            return statusCategories.contains("DEAD");
                        });
    }

    @Test
    void addBurningStateToActor() throws InterruptedException {
        // Wait for kafka to fully initialise
        // TODO: can change this to check dynamically when topics are ready, through something like:
        //  ListTopicsResult topics = adminClient.listTopics();
        Thread.sleep(1000);

        Instant expiration = Instant.now().plusMillis(2000);
        double damage = 40.0;
        Status burning = new Burning(expiration, TEST_ACTOR_2, damage, 1, "Fireball");

        Stats initialStats = statsService.getStatsFor(TEST_ACTOR).blockingGet();
        Double initialHp = initialStats.getDerived(StatsTypes.CURRENT_HP);
        String out = String.format("Initial HP: %s", initialHp);
        System.out.println(out);

        // Add padding to damage due to HP regen
        Double expectedLife =
                initialStats.getDerived(StatsTypes.CURRENT_HP) - (damage * 3) + (damage * 0.7);

        ActorStatus initialStatus = new ActorStatus(TEST_ACTOR, new HashSet<>(), false, Set.of());

        // when
        statusService.addStatusToActor(initialStatus, Set.of(burning));

        ConcurrentMap<String, WebSocketSession> testSessionData = new ConcurrentHashMap<>();
        testSessionData.put(TEST_ACTOR, session);
        Mockito.when(sessionParamHelper.getLiveSessions()).thenReturn(testSessionData);

        // then
        await().ignoreExceptions()
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(3, ChronoUnit.SECONDS))
                .until(
                        () -> {
                            ActorStatus actorStatus =
                                    statusService
                                            .getActorStatus(TEST_ACTOR)
                                            .doOnError(err -> System.out.println(err.getMessage()))
                                            .blockingGet();
                            actorStatus.aggregateStatusEffects();
                            return actorStatus.getStatusEffects().contains(burning.getCategory());
                        });

        System.out.println("Burning status detected, checking damage");

        await().pollDelay(200, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .timeout(Duration.of(3, ChronoUnit.SECONDS))
                .until(
                        () -> {
                            Stats actorStats =
                                    statsService
                                            .getStatsFor(TEST_ACTOR)
                                            .doOnError(err -> System.out.println(err.getMessage()))
                                            .blockingGet();
                            Double currentHp = actorStats.getDerived(StatsTypes.CURRENT_HP);
                            Double diff = initialHp - currentHp;
                            String out2 = String.format("HP now: %s, diff: %s", currentHp, diff);
                            System.out.println(out2);

                            return actorStats.getDerived(StatsTypes.CURRENT_HP) < expectedLife;
                        });
    }
}
