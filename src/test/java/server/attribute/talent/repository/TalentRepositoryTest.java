package server.attribute.talent.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.attribute.common.model.AttributeApplyType;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier1.SharpenedBlades;
import server.attribute.talents.available.melee.fighter.weaponmaster.tier2.CripplingBlows;
import server.attribute.talents.model.ActorTalents;
import server.attribute.talents.model.Talent;
import server.attribute.talents.repository.TalentRepository;

@MicronautTest
public class TalentRepositoryTest {

    @Inject TalentRepository talentRepository;

    private static final String TEST_PLAYER = "testPlayer123";

    @BeforeEach
    void clearData() {
        clean();
    }

    @AfterEach
    void cleanData() {
        clean();
    }

    private void clean() {
        talentRepository.deleteActorTalents(TEST_PLAYER).blockingSubscribe();
    }

    @Test
    void testGetTalentByName() {
        String talentName = "Sharpened blades";
        Talent expectedTalent = new SharpenedBlades();

        Talent actual = talentRepository.getTalentByName(talentName);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expectedTalent);
    }

    @Test
    void testGetActorTalents() {
        // given
        ActorTalents expectedTalents = new ActorTalents(TEST_PLAYER, Map.of("Sharpened blades", 2));
        talentRepository.insertActorTalents(TEST_PLAYER, expectedTalents).blockingSubscribe();

        // when
        ActorTalents result = talentRepository.getActorTalents(TEST_PLAYER).blockingGet();

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedTalents);
    }

    @Test
    void testGetTalentByType() {
        ActorTalents actorTalents =
                new ActorTalents(
                        TEST_PLAYER,
                        Map.of(
                                "Sharpened blades", 2,
                                "Reflex training", 3,
                                "Crippling blows", 1));

        talentRepository.insertActorTalents(TEST_PLAYER, actorTalents).blockingSubscribe();

        // when
        Map<Talent, Integer> talents =
                talentRepository
                        .getActorTalentsOfApplyType(
                                TEST_PLAYER, AttributeApplyType.ON_DMG_APPLY.getType())
                        .blockingGet();

        assertThat(talents).hasSize(1);

        Talent cripplingBlows = new CripplingBlows();
        assertThat(talents.keySet().stream().findFirst().get())
                .usingRecursiveComparison()
                .ignoringFields("rand")
                .isEqualTo(cripplingBlows);
    }
}
