package server.motion.service;

import com.mongodb.client.result.UpdateResult;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.common.dto.Motion;
import server.player.character.dto.AccountCharactersResponse;
import server.player.character.dto.Character;
import server.player.character.dto.CreateCharacterRequest;
import server.player.character.repository.PlayerCharacterRepository;
import server.player.character.service.PlayerCharacterService;
import server.player.motion.dto.PlayerMotion;
import server.player.motion.service.PlayerMotionService;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@MicronautTest
public class PlayerMotionServiceTest {

    @Inject
    PlayerCharacterService playerCharacterService;

    @Inject
    PlayerCharacterRepository playerCharacterRepository;

    @Inject
    PlayerMotionService playerMotionService;

    private static String TEST_USERNAME = "USER";
    private static String TEST_CHARACTER_NAME = "CHARACTER";

    @BeforeEach
    void cleanDb() {
        playerCharacterRepository.deleteByCharacterName(TEST_CHARACTER_NAME).blockingGet();
    }

    @Test
    void testWhenCharacterIsCreatedTheMotionCanBeUpdatedAsExpected() {
        // Given
        CreateCharacterRequest createCharacterRequest = new CreateCharacterRequest();
        createCharacterRequest.setName(TEST_CHARACTER_NAME);
        Map<String, String> appearanceInfo = Map.of("key", "value");
        createCharacterRequest.setAppearanceInfo(appearanceInfo);

        Character testCharacter = playerCharacterService.createCharacter(createCharacterRequest, TEST_USERNAME);

        Motion motion = new Motion(1,2,3,4,5,6,7,8,9, true);
        PlayerMotion playerMotion = new PlayerMotion();
        playerMotion.setPlayerName(TEST_CHARACTER_NAME);
        playerMotion.setMotion(motion);

        testCharacter.setMotion(motion);

        LocalDateTime old = LocalDateTime.now(ZoneOffset.UTC).minusHours(2);
        testCharacter.setUpdatedAt(old);
        testCharacter.setIsOnline(true);

        // When
        UpdateResult res = playerMotionService.updatePlayerState(playerMotion);
        Character actualCharacter = playerCharacterRepository.findByName(TEST_CHARACTER_NAME).blockingGet();

        // Then
        Assertions.assertThat(actualCharacter).usingRecursiveComparison()
                .ignoringFields("updatedAt")
                .isEqualTo(testCharacter);
        Assertions.assertThat(actualCharacter.getUpdatedAt()).isAfter(old);
    }

    @Test
    void testWhenCharacterIsNearbyIsReturnedAsExpected() {
        // Given
        CreateCharacterRequest createCharacterRequest = new CreateCharacterRequest();
        createCharacterRequest.setName(TEST_CHARACTER_NAME);
        Map<String, String> appearanceInfo = Map.of("key", "value");
        createCharacterRequest.setAppearanceInfo(appearanceInfo);

        Character testCharacter = playerCharacterService.createCharacter(createCharacterRequest, TEST_USERNAME);

        Motion motion = new Motion(1,2,3,4,5,6,7,8,9, true);
        PlayerMotion playerMotion = new PlayerMotion();
        playerMotion.setPlayerName(TEST_CHARACTER_NAME);
        playerMotion.setMotion(motion);

        testCharacter.setMotion(motion);

        LocalDateTime old = LocalDateTime.now(ZoneOffset.UTC).minusHours(2);
        testCharacter.setUpdatedAt(old);
        testCharacter.setIsOnline(true);

        // one player is online and moving
        playerMotionService.updatePlayerState(playerMotion);

        // When
        // another player checks if anyone is nearby
        AccountCharactersResponse res = playerMotionService.getPlayersNearMe("somePlayer");

        // Then
        Assertions.assertThat(res.getAccountCharacters().size()).isEqualTo(1);
        Assertions.assertThat(res.getAccountCharacters().get(0)).usingRecursiveComparison()
                .ignoringFields("updatedAt")
                .isEqualTo(testCharacter);
    }
}
