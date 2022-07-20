package server.player.attributes.levels.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import server.common.dto.NumTag;
import server.player.attributes.helpers.PlayerAttributeTestHelper;
import server.player.attributes.levels.types.ClassesAttributeTypes;
import server.player.attributes.levels.types.LevelAttributeTypes;
import server.player.attributes.model.PlayerAttributes;
import server.player.attributes.repository.PlayerAttributesRepository;
import server.player.attributes.service.PlayerAttributeService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static server.player.attributes.types.AttributeTypes.*;
import static server.player.attributes.types.AttributeTypes.INT;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerLevelAttributeServiceTest {

    @Inject
    PlayerLevelAttributeService playerLevelAttributeService;

    @Inject
    PlayerAttributeService attributeService;

    @Inject
    PlayerAttributeTestHelper playerAttributeTestHelper;

    @Inject
    PlayerAttributesRepository attributesRepository;

    private static final String CHARACTER_NAME = "TEST_PLAYER";


    private static Stream<Arguments> availableClasses() {
        return Stream.of(
                Arguments.of(ClassesAttributeTypes.CLERIC.getType()),
                Arguments.of(ClassesAttributeTypes.FIGHTER.getType()),
                Arguments.of(ClassesAttributeTypes.MAGE.getType())
        );
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
        List<NumTag> expectedTags = buildBaseExpectedNumTagsForCharacterClass(characterClass, 1);

        // when
        playerLevelAttributeService.initializeCharacterClass(CHARACTER_NAME, characterClass);

        // then
        PlayerAttributes attributes = attributeService.getPlayerAttributes(CHARACTER_NAME);
        List<NumTag> actualBaseAttr = attributes.getBaseAttributes();

        Assertions.assertThat(actualBaseAttr).containsAll(expectedTags);
    }

    @ParameterizedTest
    @MethodSource("availableClasses")
    void handleLevelUp(String characterClass) {
        // given
        attributeService.createBaseAttributes(CHARACTER_NAME);
        playerLevelAttributeService.initializeCharacterClass(CHARACTER_NAME, characterClass);

        // when
        playerLevelAttributeService.handleLevelUp(CHARACTER_NAME, characterClass);
        List<NumTag> expectedTags = buildBaseExpectedNumTagsForCharacterClass(characterClass, 2);

        // then
        PlayerAttributes attributes = attributeService.getPlayerAttributes(CHARACTER_NAME);
        List<NumTag> actualBaseAttr = attributes.getBaseAttributes();

        Assertions.assertThat(actualBaseAttr).containsAll(expectedTags);
    }

    @Test
    void addXpToCharacter() {
        // given
        attributeService.createBaseAttributes(CHARACTER_NAME);
        playerLevelAttributeService.initializeCharacterClass(CHARACTER_NAME, ClassesAttributeTypes.FIGHTER.getType());

        NumTag expectedTag = new NumTag(LevelAttributeTypes.XP.getType(), 700);

        // when
        playerLevelAttributeService.addPlayerXp(CHARACTER_NAME, 500);
        playerLevelAttributeService.addPlayerXp(CHARACTER_NAME, 200);

        // then
        PlayerAttributes attributes = attributeService.getPlayerAttributes(CHARACTER_NAME);
        List<NumTag> actualBaseAttr = attributes.getBaseAttributes();
        NumTag xpTag = PlayerAttributeService.findTag(actualBaseAttr, LevelAttributeTypes.XP.getType());

        Assertions.assertThat(xpTag).usingRecursiveComparison().isEqualTo(expectedTag);
    }

    private List<NumTag> buildBaseExpectedNumTagsForCharacterClass(String characterClass, Integer expectedLevel) {
        List<String> classesAvailable = PlayerLevelAttributeService.AVAILABLE_CLASSES;
        List<NumTag> expectedTags = new ArrayList<>();
        classesAvailable.forEach(c ->
            expectedTags.add(
                    new NumTag(
                            c,
                            c.equals(characterClass) ? expectedLevel : 0
                    )
            )
        );

        return expectedTags;
    }
}
