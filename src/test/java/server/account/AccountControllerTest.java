package server.account;

import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.org.mmo_server.repository.model.tables.Users;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.Flowable;
import org.jooq.DSLContext;
import org.junit.jupiter.api.*;
import server.account.dto.Account;
import server.account.dto.RegisterDto;

import javax.inject.Inject;
import java.text.ParseException;
import java.time.LocalDateTime;

import static com.org.mmo_server.repository.model.tables.UserRoles.USER_ROLES;
import static com.org.mmo_server.repository.model.tables.Users.USERS;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
public class AccountControllerTest {

    @Inject
    EmbeddedServer embeddedServer;
    RxHttpClient client;

    private static String REGISTER_PATH = "/stuff/register";

    private static String TEST_USERNAME = "someUsername";
    private static String TEST_PASSWORD = "somePassword";
    private static String TEST_EMAIL = "someEmail";


    @Inject
    DSLContext dslContext;


    @BeforeAll
    void setup() {
        client  = embeddedServer.getApplicationContext()
                .createBean(RxHttpClient.class, embeddedServer.getURL());
    }

    @BeforeEach
    void setupDatabase() {
        deleteUserIfExist();
    }

    @AfterAll
    void cleanUp() {
        deleteUserIfExist();
    }

    private void deleteUserIfExist() {
        dslContext.deleteFrom(USERS).where(USERS.USERNAME.equal(TEST_USERNAME));
    }

    @Test
    void testRegisterEndpointCreatesUser() {
        // given
        RegisterDto registerDto = new RegisterDto(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);

        // when
        HttpRequest request = HttpRequest.POST(REGISTER_PATH, registerDto);

        HttpResponse<Account> rsp =
                client.toBlocking().exchange(request, Account.class);

        Account accountResponse = rsp.body();

        Assertions.assertNotNull(accountResponse);
        Assertions.assertEquals(TEST_USERNAME, accountResponse.getUsername());
        Assertions.assertEquals(TEST_EMAIL, accountResponse.getEmail());
    }
}