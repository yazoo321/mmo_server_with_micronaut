package server.player.character.repository;

import com.mongodb.client.result.DeleteResult;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.player.character.dto.Character;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
public class PlayerCharacterRepositoryTest {

    @Inject
    PlayerCharacterRepository playerCharacterRepository;

    private static final String CHAR_1 = "char1";
    private static final String CHAR_2 = "char2";
    private static final String CHAR_3 = "char3";

    private static final String ACC_1 = "account_1";
    private static final String ACC_2 = "account_2";

    @BeforeEach
    void cleanup() {
        List<String> namesToDelete = List.of(CHAR_1, CHAR_2, CHAR_3);
        namesToDelete.forEach(name ->
                playerCharacterRepository.deleteByCharacterName(name).blockingGet());
    }

    @Test
    void testSaveCharacterAndGetCharacterForUser() {
        // given
        Character character = createCharacter(CHAR_1, ACC_1);

        // when
        Single<Character> saved = playerCharacterRepository.save(character);

        // then
        Maybe<Character> found = playerCharacterRepository.findByName(CHAR_1);

        Assertions.assertEquals(CHAR_1, saved.blockingGet().getName());
        Assertions.assertEquals(CHAR_1, found.blockingGet().getName());
    }

    @Test
    void testGetCharacterByAccountWorksAsExpected() {
        // given
        Character character1 = createCharacter(CHAR_1, ACC_1);
        Character character2 = createCharacter(CHAR_2, ACC_1);
        Character character3 = createCharacter(CHAR_3, ACC_2);

        playerCharacterRepository.save(character1).blockingGet();
        playerCharacterRepository.save(character2).blockingGet();
        playerCharacterRepository.save(character3).blockingGet();

        // when
        List<Character> foundAccount1 =
                playerCharacterRepository.findByAccount(ACC_1).blockingGet();

        List<Character> foundAccount2 =
                playerCharacterRepository.findByAccount(ACC_2).blockingGet();

        // then
        Assertions.assertEquals(2, foundAccount1.size());
        Assertions.assertEquals(1, foundAccount2.size());

        List<String> expectedNames1 = List.of(CHAR_1, CHAR_2);
        foundAccount1.forEach(acc ->
                Assertions.assertTrue(expectedNames1.contains(acc.getName())));

        Assertions.assertEquals(CHAR_3, foundAccount2.get(0).getName());
    }

    @Test
    void testDeleteCharacter() {
        // given
        Character character1 = createCharacter(CHAR_1, ACC_1);
        playerCharacterRepository.save(character1).blockingGet();

        // ensure there's an entry
        Assertions.assertEquals(CHAR_1,
                playerCharacterRepository.findByName(CHAR_1).blockingGet().getName());
        // when
        DeleteResult res = playerCharacterRepository.deleteByCharacterName(CHAR_1).blockingGet();

        // then
        Assertions.assertEquals(1, res.getDeletedCount());
        Assertions.assertNull(playerCharacterRepository.findByName(CHAR_1).blockingGet());
    }

    @Test
    void testCreateDoesNotCauseDuplicateRecords() {
        // given
        Character character = createCharacter(CHAR_1, ACC_1);
        playerCharacterRepository.save(character).blockingGet();

        // when
        Single<Character> characterDup = playerCharacterRepository.createNew(character);

        // then
        Assertions.assertThrows(NullPointerException.class, characterDup::blockingGet);
    }

    @Test
    void testCheckAndUpdateUserOnline() {
        // given
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        Character character1 = createCharacter(CHAR_1, ACC_1);
        Character character2 = createCharacter(CHAR_2, ACC_1);
        Character character3 = createCharacter(CHAR_3, ACC_2);

        character1.setIsOnline(true);
        character2.setIsOnline(true);
        character3.setIsOnline(true);

        character1.setUpdatedAt(now);
        character2.setUpdatedAt(now.minusSeconds(5));
        character3.setUpdatedAt(now.minusSeconds(15));

        // blocking get in order to sync up the saves with the test
        playerCharacterRepository.save(character1).blockingGet();
        playerCharacterRepository.save(character2).blockingGet();
        playerCharacterRepository.save(character3).blockingGet();

        // when
        playerCharacterRepository.checkAndUpdateUserOnline();

        // then
        Character actualCharacter1 = playerCharacterRepository.findByName(character1.getName()).blockingGet();
        Character actualCharacter2 = playerCharacterRepository.findByName(character1.getName()).blockingGet();
        Character actualCharacter3 = playerCharacterRepository.findByName(character1.getName()).blockingGet();

        character3.setIsOnline(false);

        assertThat(actualCharacter1).usingRecursiveComparison()
                .ignoringFields("updatedAt")
                .isEqualTo(character1);
        assertThat(actualCharacter2).usingRecursiveComparison()
                .ignoringFields("updatedAt")
                .isEqualTo(character1);
        assertThat(actualCharacter3).usingRecursiveComparison()
                .ignoringFields("updatedAt")
                .isEqualTo(character1);
    }

    private Character createCharacter(String characterName, String account) {
        Character character = new Character();
        character.setName(characterName);
        character.setXp(0);
        character.setAccountName(account);
        character.setAppearanceInfo(Map.of("key", "value"));
        return character;
    }
}
