package server.player.character.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import server.player.character.repository.PlayerCharacterRepository;

import javax.inject.Inject;

@MicronautTest
public class PlayerCharacterServiceTest {

    // This test will be very similar to
    // PlayerCharacterRepository test as there's limited functionality
    @Inject
    PlayerCharacterService playerCharacterService;

    @Inject
    PlayerCharacterRepository playerCharacterRepository;

    @Test
    void testSaveCharacterAndGetCharacterForUser() {

    }

}
