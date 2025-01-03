package server.combat.repository;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import server.combat.model.ActorThreat;

import java.util.Map;

@MicronautTest
class ActorThreatRepositoryIntegrationTest {

    @Inject
    private ActorThreatRepository repository;

    private static String actorId1 = "testActor1";
    private static String actorId2 = "testActor2";


    @AfterEach
    void setUp() {
        repository.resetActorThreat(actorId1).blockingSubscribe();
        repository.resetActorThreat(actorId2).blockingSubscribe();
    }

    @Test
    void addThreatToActorWhenItDoesntExist() {
        // given
        int threatToAdd = 100;
        ActorThreat expected = new ActorThreat();
        expected.setActorId(actorId1);
        Map<String, Integer> expectedThreatMap = Map.of(actorId2, threatToAdd);
        expected.setActorThreat(expectedThreatMap);

        // when
        repository.addThreatToActor(actorId1, actorId2, threatToAdd).blockingSubscribe();

        // then
        ActorThreat actorThreat = repository.fetchActorThreat(actorId1).blockingGet();

        Assertions.assertThat(actorThreat).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void addThreatToActorWhenItAlreadyExists() {
        // given
        int threatToAdd = 100;
        ActorThreat expected = new ActorThreat();
        expected.setActorId(actorId1);
        Map<String, Integer> expectedThreatMap = Map.of(actorId2, threatToAdd * 2);
        expected.setActorThreat(expectedThreatMap);

        // when
        repository.addThreatToActor(actorId1, actorId2, threatToAdd).blockingSubscribe();
        repository.addThreatToActor(actorId1, actorId2, threatToAdd).blockingSubscribe();

        // then
        ActorThreat actorThreat = repository.fetchActorThreat(actorId1).blockingGet();

        Assertions.assertThat(actorThreat).usingRecursiveComparison().isEqualTo(expected);
    }


}