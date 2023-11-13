package server.attribute.stats.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import server.attribute.stats.helpers.StatsTestHelper;
import server.attribute.stats.model.Stats;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.StatsTypes;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerLevelStatsServiceTest {

    @Inject PlayerLevelStatsService playerLevelStatsService;

    @Inject StatsTestHelper statsTestHelper;

    @Inject StatsService statsService;

    private static final String CHARACTER_NAME = "TEST_PLAYER";

    private static Stream<Arguments> availableClasses() {
        return Stream.of(
                Arguments.of(ClassTypes.CLERIC.getType()),
                Arguments.of(ClassTypes.FIGHTER.getType()),
                Arguments.of(ClassTypes.MAGE.getType()));
    }

    @BeforeEach
    void cleanup() {
        statsTestHelper.deleteAllAttributeData();
    }

    @AfterAll
    void clean() {
        statsTestHelper.deleteAllAttributeData();
    }

    @ParameterizedTest
    @MethodSource("availableClasses")
    void whenCreatingNewCharacterOfClassTheDataIsSetupAsExpected(String characterClass) {
        // given
        // user has to pre-initialize
        statsService.initializePlayerStats(CHARACTER_NAME).blockingSubscribe();
        Map<String, Integer> expectedTags =
                buildBaseExpectedNumTagsForCharacterClass(characterClass, 1);

        // when
        playerLevelStatsService
                .initializeCharacterClass(CHARACTER_NAME, characterClass)
                .blockingSubscribe();

        // then
        Stats stats = statsService.getStatsFor(CHARACTER_NAME).blockingGet();
        Map<String, Integer> actualBaseAttr = stats.getBaseStats();

        Assertions.assertThat(actualBaseAttr).containsAllEntriesOf(expectedTags);
    }

    @ParameterizedTest
    @MethodSource("availableClasses")
    void handleLevelUp(String characterClass) {
        // given
        statsService.initializePlayerStats(CHARACTER_NAME).blockingSubscribe();
        playerLevelStatsService
                .initializeCharacterClass(CHARACTER_NAME, characterClass)
                .blockingSubscribe();

        // when
        playerLevelStatsService.handleLevelUp(CHARACTER_NAME, characterClass).blockingSubscribe();
        Map<String, Integer> expectedTags =
                buildBaseExpectedNumTagsForCharacterClass(characterClass, 2);

        // then
        Stats stats = statsService.getStatsFor(CHARACTER_NAME).blockingGet();
        Map<String, Integer> actualBaseAttr = stats.getBaseStats();

        Assertions.assertThat(actualBaseAttr).containsAllEntriesOf(expectedTags);
    }

    @Test
    void addXpToCharacter() {
        // given
        statsService.initializePlayerStats(CHARACTER_NAME).blockingSubscribe();
        playerLevelStatsService
                .initializeCharacterClass(CHARACTER_NAME, ClassTypes.FIGHTER.getType())
                .blockingSubscribe();

        Double expectedXp = 700.0;

        // when
        playerLevelStatsService.addPlayerXp(CHARACTER_NAME, 500).blockingSubscribe();
        playerLevelStatsService.addPlayerXp(CHARACTER_NAME, 200).blockingSubscribe();

        // then
        Stats stats = statsService.getStatsFor(CHARACTER_NAME).blockingGet();

        Double actualXp = stats.getDerived(StatsTypes.XP);

        Assertions.assertThat(actualXp).isEqualTo(expectedXp);
    }

    private Map<String, Integer> buildBaseExpectedNumTagsForCharacterClass(
            String characterClass, Integer expectedLevel) {
        List<String> classesAvailable = PlayerLevelStatsService.AVAILABLE_CLASSES;
        Map<String, Integer> expectedTags = new HashMap<>();
        classesAvailable.forEach(
                c -> expectedTags.put(c, c.equals(characterClass) ? expectedLevel : 0));

        return expectedTags;
    }
}
