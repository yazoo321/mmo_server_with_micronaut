package server.player.attributes.service;

import static server.player.attributes.types.AttributeTypes.*;
import static server.player.attributes.types.AttributeTypes.CAST_SPEED;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.HashMap;
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
import server.player.attributes.helpers.PlayerAttributeTestHelper;
import server.player.attributes.model.PlayerAttributes;
import server.player.attributes.repository.PlayerAttributesRepository;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerAttributeServiceTest {

    @Inject PlayerAttributeTestHelper playerAttributeTestHelper;

    @Inject PlayerAttributeService attributeService;

    @Inject PlayerAttributesRepository attributesRepository;

    private static final String CHARACTER_NAME = "TEST_PLAYER";

    @BeforeEach
    void cleanup() {
        playerAttributeTestHelper.deleteAllAttributeData();
    }

    @AfterAll
    void clean() {
        playerAttributeTestHelper.deleteAllAttributeData();
    }

    @Test
    void createBaseAttributesWillCreateAllAsExpected() {
        // given
        PlayerAttributes expected = buildExpectedAttributes();

        // when
        attributeService.createBaseAttributes(CHARACTER_NAME);

        // then
        PlayerAttributes actual = attributesRepository.findPlayerAttributes(CHARACTER_NAME);

        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    private static Stream<Arguments> attributesToAdd() {
        return Stream.of(
                Arguments.of(STR.type),
                Arguments.of(DEX.type),
                Arguments.of(STA.type),
                Arguments.of(INT.type));
    }

    @ParameterizedTest
    @MethodSource("attributesToAdd")
    void testAddingAttributeWillAddItToBaseAndCount(String attributeAdded) {
        // given
        PlayerAttributes playerAttributes = attributeService.createBaseAttributes(CHARACTER_NAME);

        Integer baseTag = playerAttributes.getBaseAttributes().get(attributeAdded);
        Integer addedTag = playerAttributes.getAttributesAdded().get(attributeAdded);
        Integer currentTag = playerAttributes.getCurrentAttributes().get(attributeAdded);

        // when
        attributeService.addPlayerAttribute(CHARACTER_NAME, attributeAdded);

        // then
        PlayerAttributes actualAttributes = attributeService.getPlayerAttributes(CHARACTER_NAME);

        Integer actualBase = actualAttributes.getBaseAttributes().get(attributeAdded);
        Integer actualAdded = actualAttributes.getAttributesAdded().get(attributeAdded);
        Integer actualCurrent = actualAttributes.getCurrentAttributes().get(attributeAdded);

        Assertions.assertThat(actualBase).isEqualTo(baseTag + 1);
        Assertions.assertThat(actualAdded).isEqualTo(addedTag + 1);
        Assertions.assertThat(actualCurrent).isEqualTo(currentTag + 1);
    }

    private static Stream<Arguments> modifyAttributes() {
        return Stream.of(
                // Base attributes
                Arguments.of(STR.type, 5),
                Arguments.of(STR.type, -5),
                Arguments.of(DEX.type, 5),
                Arguments.of(DEX.type, -5),
                Arguments.of(STA.type, 5),
                Arguments.of(STA.type, -5),
                Arguments.of(INT.type, 5),
                Arguments.of(INT.type, -5),

                // others
                Arguments.of(HP.type, 5),
                Arguments.of(HP.type, -5),
                Arguments.of(MP.type, 5),
                Arguments.of(MP.type, -5),
                Arguments.of(ATTACK_SPEED.type, 25),
                Arguments.of(ATTACK_SPEED.type, -25),
                Arguments.of(CAST_SPEED.type, 25),
                Arguments.of(CAST_SPEED.type, -25),
                Arguments.of(PHY_AMP.type, 25),
                Arguments.of(PHY_AMP.type, -25),
                Arguments.of(MAG_AMP.type, 25),
                Arguments.of(MAG_AMP.type, -25),
                Arguments.of(PHY_CRIT.type, 25),
                Arguments.of(PHY_CRIT.type, -25),
                Arguments.of(MGC_CRIT.type, 25),
                Arguments.of(MGC_CRIT.type, -25));
    }

    @ParameterizedTest
    @MethodSource("modifyAttributes")
    void testModifyCurrentAttributesWillChangeThemAsExpected(
            String attributeAdded, Integer amount) {
        // given
        PlayerAttributes playerAttributes = attributeService.createBaseAttributes(CHARACTER_NAME);

        Integer currentValue = playerAttributes.getCurrentAttributes().get(attributeAdded);

        // when
        attributeService.modifyCurrentAttribute(CHARACTER_NAME, attributeAdded, amount);

        // then
        PlayerAttributes actualAttributes = attributeService.getPlayerAttributes(CHARACTER_NAME);

        Integer actualCurrent = actualAttributes.getCurrentAttributes().get(attributeAdded);

        Assertions.assertThat(actualCurrent).isEqualTo(currentValue + amount);
    }

    private PlayerAttributes buildExpectedAttributes() {
        Map<String, Integer> baseAttributes =
                new HashMap<>(
                        Map.of(
                                STR.type, 10,
                                DEX.type, 10,
                                STA.type, 10,
                                INT.type, 10));

        Map<String, Integer> added =
                new HashMap<>(
                        Map.of(
                                STR.type, 0,
                                DEX.type, 0,
                                STA.type, 0,
                                INT.type, 0));

        Map<String, Integer> current =
                new HashMap<>(
                        Map.of(
                                HP.type, 100,
                                MP.type, 100,
                                PHY_AMP.type, 0,
                                MAG_AMP.type, 0,
                                DEF.type, 10,
                                MAG_DEF.type, 10,
                                ATTACK_SPEED.type, 50,
                                CAST_SPEED.type, 50,
                                PHY_CRIT.type, 5,
                                MGC_CRIT.type, 5));

        current.putAll(baseAttributes);

        return PlayerAttributes.builder()
                .playerName(CHARACTER_NAME)
                .baseAttributes(baseAttributes)
                .attributesAdded(added)
                .currentAttributes(current)
                .build();
    }
}
