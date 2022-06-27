package server.player.controller;

import com.org.mmo_server.repository.model.tables.UserRoles;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.jooq.DSLContext;
import org.junit.jupiter.api.*;
import server.player.character.dto.Character;
import server.player.character.dto.CreateCharacterRequest;
import server.player.character.repository.PlayerCharacterRepository;
import server.security.BCryptPasswordEncoderService;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.org.mmo_server.repository.model.tables.UserRoles.USER_ROLES;
import static com.org.mmo_server.repository.model.tables.Users.USERS;

// This test is disabled because authentication is disabled
@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
public class PlayerControllerTest {

    @Inject
    EmbeddedServer embeddedServer;
    RxHttpClient client;

    @Inject
    DSLContext dslContext;

    @Inject
    BCryptPasswordEncoderService bCryptPasswordEncoderService;

    @Inject
    PlayerCharacterRepository playerCharacterRepository;

    private static String CREATE_CHARACTER_PATH = "/player/create-character";
    private static String GET_CHARACTERS_PATH = "/player/account-characters";

    private static String VALID_USERNAME = "someUsername";
    private static String VALID_PASSWORD = "somePassword";
    private static String VALID_EMAIL = "someEmail";
    private static String ROLE_USER = "ROLE_USER";

    private static String TEST_CHARACTER_NAME = "CharacterName";

    @BeforeAll
    void setup() {
        client  = embeddedServer.getApplicationContext()
                .createBean(RxHttpClient.class, embeddedServer.getURL());
        cleanDb();
        seedDb();
    }

    @AfterAll
    void clean() {
        cleanDb();
    }

    @Test
    void testCreateCharacterAndGetCharactersForUserWorksAsExpected() {
        // this is not vanilla test, we will combine them in one, similar to BDD/integration test
        // given
        // make sure character does not already exist
        playerCharacterRepository.deleteByCharacterName(TEST_CHARACTER_NAME);

        String bearerToken = getBearerTokenForUser();
        CreateCharacterRequest createCharacterRequest = new CreateCharacterRequest();
        createCharacterRequest.setName(TEST_CHARACTER_NAME);
        Map<String, String> appearanceInfo = Map.of("key", "value");
        createCharacterRequest.setAppearanceInfo(appearanceInfo);

        // make sure character does not exist
        Assertions.assertNull(playerCharacterRepository.findByName(createCharacterRequest.getName()));

        // when
        HttpRequest requestCreateCharacter = HttpRequest.POST(CREATE_CHARACTER_PATH, createCharacterRequest)
                .bearerAuth(bearerToken);
        HttpResponse<Character> rspCreateChar =
                client.toBlocking().exchange(requestCreateCharacter, Character.class);

        // then
        Character createdCharacter = rspCreateChar.getBody().get();
        Assertions.assertNotNull(createdCharacter);
        Assertions.assertEquals(createCharacterRequest.getName(), createdCharacter.getName());
        Assertions.assertEquals(createCharacterRequest.getAppearanceInfo(), createdCharacter.getAppearanceInfo());

        // when
        HttpRequest requestGetCharacters = HttpRequest.GET(GET_CHARACTERS_PATH).bearerAuth(bearerToken);
        var rspWithChars =
                client.toBlocking().exchange(requestGetCharacters, List.class);

        // complicated data structure due to bad deserialization
        var characterList = (LinkedHashMap) ((List)rspWithChars.getBody().get()).get(0);

        // then
        Assertions.assertNotNull(characterList);
        LinkedHashMap character = ((List<LinkedHashMap>) characterList.get("accountCharacters")).get(0);

        Assertions.assertEquals(createCharacterRequest.getName(), character.get("name"));
        Assertions.assertEquals(createCharacterRequest.getAppearanceInfo(), character.get("appearanceInfo"));
        Assertions.assertEquals(VALID_USERNAME, character.get("accountName"));
    }

    private String getBearerTokenForUser() {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(VALID_USERNAME, VALID_PASSWORD);

        HttpRequest request = HttpRequest.POST("/login", creds);
        HttpResponse<BearerAccessRefreshToken> rsp =
                client.toBlocking().exchange(request, BearerAccessRefreshToken.class);

        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body();
        return bearerAccessRefreshToken.getAccessToken();
    }

    private void seedDb() {
        String encryptedPw = bCryptPasswordEncoderService.encode(VALID_PASSWORD);
        LocalDateTime now = LocalDateTime.now();
        dslContext.insertInto(USERS)
                .columns(USERS.USERNAME, USERS.EMAIL, USERS.PASSWORD,
                        USERS.ENABLED, USERS.CREATED_AT, USERS.UPDATED_AT,
                        USERS.LAST_LOGGED_IN_AT)
                .values(VALID_USERNAME, VALID_EMAIL, encryptedPw,
                        true, now, now, now).execute();

        dslContext.insertInto(USER_ROLES)
                .columns(USER_ROLES.USERNAME, USER_ROLES.ROLE)
                .values(VALID_USERNAME, ROLE_USER).execute();
    }

    private void cleanDb() {
        dslContext.deleteFrom(UserRoles.USER_ROLES)
                .where(UserRoles.USER_ROLES.USERNAME.equal(VALID_USERNAME)).execute();
        dslContext.deleteFrom(USERS).where(USERS.USERNAME.equal(VALID_USERNAME)).execute();
    }
}
