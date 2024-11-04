package server.faction.repository;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.faction.model.ActorAllegiance;

import java.util.List;


@MicronautTest
public class ActorAllegianceRepositoryTest {

    @Inject
    private ActorAllegianceRepository actorAllegianceRepository;

    private final ActorAllegiance testActorAllegiance = new ActorAllegiance("actor123", "GuildName");

    @BeforeEach
    void setUp() {
        actorAllegianceRepository.insert(testActorAllegiance.getActorId(), testActorAllegiance.getAllegianceName())
                .blockingSubscribe();
    }

    @Test
    void testFindByActorId() {
        List<ActorAllegiance> actual = actorAllegianceRepository.findByActorId("actor123").blockingGet();

        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(List.of(testActorAllegiance));
    }
}
