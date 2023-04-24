package server.player.attributes.levels.service;

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
import server.common.attributes.types.ClassesAttributeTypes;
import server.common.attributes.types.LevelAttributeTypes;
import server.player.attributes.helpers.PlayerAttributeTestHelper;
import server.player.attributes.model.PlayerAttributes;
import server.player.attributes.repository.PlayerAttributesRepository;
import server.player.attributes.service.PlayerAttributeService;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerLevelAttributeServiceTest {

    @Inject PlayerLevelAttributeService playerLevelAttributeService;

    @Inject PlayerAttributeService attributeService;

    @Inject PlayerAttributeTestHelper playerAttributeTestHelper;

    @Inject PlayerAttributesRepository attributesRepository;

    private static final String CHARACTER_NAME = "TEST_PLAYER";

    private static Stream<Arguments> availableClasses() {
        return Stream.of(
                Arguments.of(ClassesAttributeTypes.CLERIC.getType()),
                Arguments.of(ClassesAttributeTypes.FIGHTER.getType()),
                Arguments.of(ClassesAttributeTypes.MAGE.getType()));
    }

    @BeforeEach
    void cleanup() {
        playerAttributeTestHelper.deleteAllAttributeData();
    }

    @AfterAll
    void clean() {
        playerAttributeTestHelper.deleteAllAttributeData();
    }

    @ParameterizedTest
    @MethodSource("availableClasses")
    void whenCreatingNewCharacterOfClassTheDataIsSetupAsExpected(String characterClass) {
        // given
        // user has to pre-initialize
        attributeService.createBaseAttributes(CHARACTER_NAME);
        Map<String, Integer> expectedTags =
                buildBaseExpectedNumTagsForCharacterClass(characterClass, 1);

        // when
        playerLevelAttributeService.initializeCharacterClass(CHARACTER_NAME, characterClass);

        // then
        PlayerAttributes attributes = attributeService.getPlayerAttributes(CHARACTER_NAME);
        Map<String, Integer> actualBaseAttr = attributes.getBaseAttributes();

        Assertions.assertThat(actualBaseAttr).containsAllEntriesOf(expectedTags);
    }

    @ParameterizedTest
    @MethodSource("availableClasses")
    void handleLevelUp(String characterClass) {
        // given
        attributeService.createBaseAttributes(CHARACTER_NAME);
        playerLevelAttributeService.initializeCharacterClass(CHARACTER_NAME, characterClass);

        // when
        playerLevelAttributeService.handleLevelUp(CHARACTER_NAME, characterClass);
        Map<String, Integer> expectedTags =
                buildBaseExpectedNumTagsForCharacterClass(characterClass, 2);

        // then
        PlayerAttributes attributes = attributeService.getPlayerAttributes(CHARACTER_NAME);
        Map<String, Integer> actualBaseAttr = attributes.getBaseAttributes();

        Assertions.assertThat(actualBaseAttr).containsAllEntriesOf(expectedTags);
    }

    @Test
    void addXpToCharacter() {
        // given
        attributeService.createBaseAttributes(CHARACTER_NAME);
        playerLevelAttributeService.initializeCharacterClass(
                CHARACTER_NAME, ClassesAttributeTypes.FIGHTER.getType());

        Integer expectedXp = 700;

        // when
        playerLevelAttributeService.addPlayerXp(CHARACTER_NAME, 500);
        playerLevelAttributeService.addPlayerXp(CHARACTER_NAME, 200);

        // then
        PlayerAttributes attributes = attributeService.getPlayerAttributes(CHARACTER_NAME);
        Map<String, Integer> actualBaseAttr = attributes.getBaseAttributes();
        Integer actualXp = actualBaseAttr.get(LevelAttributeTypes.XP.getType());

        Assertions.assertThat(actualXp).isEqualTo(expectedXp);
    }

    private Map<String, Integer> buildBaseExpectedNumTagsForCharacterClass(
            String characterClass, Integer expectedLevel) {
        List<String> classesAvailable = PlayerLevelAttributeService.AVAILABLE_CLASSES;
        Map<String, Integer> expectedTags = new HashMap<>();
        classesAvailable.forEach(
                c -> expectedTags.put(c, c.equals(characterClass) ? expectedLevel : 0));

        return expectedTags;
    }
}
