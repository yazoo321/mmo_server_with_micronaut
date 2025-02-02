package server.attribute.talent.repository;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import server.attribute.talents.model.ActorTalents;
import server.attribute.talents.model.Talent;
import server.attribute.talents.repository.TalentRepositoryImpl;

import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@MicronautTest
public class TalentRepositoryImplTest {

    @Inject
    TalentRepositoryImpl talentRepository;

    private static final String TEST_PLAYER = "testPlayer123";

    @Test
    void testTalentPreloading() {
        Map<String, Talent> allTalents = talentRepository.getAllTalents();

        assertThat(allTalents)
                .isNotEmpty()
                .containsKeys("Sharpened blades");
    }

    @Test
    void testGetActorTalents() {
        // given
        ActorTalents expectedTalents = new ActorTalents(TEST_PLAYER, Map.of("Sharpened blades", 2));
        talentRepository.insertActorTalents(TEST_PLAYER, expectedTalents).blockingSubscribe();

        // when
        ActorTalents result = talentRepository.getActorTalents(TEST_PLAYER).blockingGet();

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedTalents);
    }

}
