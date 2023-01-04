package server.player.attributes.service;

import static server.player.attributes.types.AttributeTypes.*;
import static server.player.attributes.types.AttributeTypes.CAST_SPEED;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
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

        NumTag baseTag =
                PlayerAttributeService.findTag(
                        playerAttributes.getBaseAttributes(), attributeAdded);
        NumTag addedTag =
                PlayerAttributeService.findTag(
                        playerAttributes.getAttributesAdded(), attributeAdded);
        NumTag currentTag =
                PlayerAttributeService.findTag(
                        playerAttributes.getCurrentAttributes(), attributeAdded);

        // when
        attributeService.addPlayerAttribute(CHARACTER_NAME, attributeAdded);

        // then
        PlayerAttributes actualAttributes = attributeService.getPlayerAttributes(CHARACTER_NAME);

        NumTag actualBase =
                PlayerAttributeService.findTag(
                        actualAttributes.getBaseAttributes(), attributeAdded);
        NumTag actualAdded =
                PlayerAttributeService.findTag(
                        actualAttributes.getAttributesAdded(), attributeAdded);
        NumTag actualCurrent =
                PlayerAttributeService.findTag(
                        actualAttributes.getCurrentAttributes(), attributeAdded);

        Assertions.assertThat(actualBase.getValue()).isEqualTo(baseTag.getValue() + 1);
        Assertions.assertThat(actualAdded.getValue()).isEqualTo(addedTag.getValue() + 1);
        Assertions.assertThat(actualCurrent.getValue()).isEqualTo(currentTag.getValue() + 1);
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

        NumTag currentTag =
                PlayerAttributeService.findTag(
                        playerAttributes.getCurrentAttributes(), attributeAdded);

        // when
        attributeService.modifyCurrentAttribute(CHARACTER_NAME, attributeAdded, amount);

        // then
        PlayerAttributes actualAttributes = attributeService.getPlayerAttributes(CHARACTER_NAME);

        NumTag actualCurrent =
                PlayerAttributeService.findTag(
                        actualAttributes.getCurrentAttributes(), attributeAdded);

        Assertions.assertThat(actualCurrent.getValue()).isEqualTo(currentTag.getValue() + amount);
    }

    private PlayerAttributes buildExpectedAttributes() {
        List<NumTag> baseAttributes =
                new ArrayList<>(
                        List.of(
                                new NumTag(STR.type, 10),
                                new NumTag(DEX.type, 10),
                                new NumTag(STA.type, 10),
                                new NumTag(INT.type, 10)));

        List<NumTag> added =
                new ArrayList<>(
                        List.of(
                                new NumTag(STR.type, 0),
                                new NumTag(DEX.type, 0),
                                new NumTag(STA.type, 0),
                                new NumTag(INT.type, 0)));

        List<NumTag> current =
                new ArrayList<>(
                        List.of(
                                new NumTag(HP.type, 100),
                                new NumTag(MP.type, 100),
                                new NumTag(PHY_AMP.type, 0),
                                new NumTag(MAG_AMP.type, 0),
                                new NumTag(DEF.type, 10),
                                new NumTag(MAG_DEF.type, 10),
                                new NumTag(ATTACK_SPEED.type, 50),
                                new NumTag(CAST_SPEED.type, 50),
                                new NumTag(PHY_CRIT.type, 5),
                                new NumTag(MGC_CRIT.type, 5)));

        current.addAll(baseAttributes);

        return PlayerAttributes.builder()
                .playerName(CHARACTER_NAME)
                .baseAttributes(baseAttributes)
                .attributesAdded(added)
                .currentAttributes(current)
                .build();
    }
}
