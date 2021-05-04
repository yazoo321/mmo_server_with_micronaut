package server.account;

import com.org.mmo_server.repository.model.tables.UserRoles;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.jooq.DSLContext;
import org.junit.jupiter.api.*;
import server.account.dto.Account;
import server.account.dto.RegisterDto;

import javax.inject.Inject;

import static com.org.mmo_server.repository.model.tables.Users.USERS;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
public class AccountControllerTest {

    @Inject
    EmbeddedServer embeddedServer;
    RxHttpClient client;

    private static String REGISTER_PATH = "/register";

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
        dslContext.deleteFrom(UserRoles.USER_ROLES)
                .where(UserRoles.USER_ROLES.USERNAME.equal(TEST_USERNAME)).execute();
        dslContext.deleteFrom(USERS).where(USERS.USERNAME.equal(TEST_USERNAME)).execute();
    }

    @Test
    void testRegisterEndpointCreatesUser() {
        // given
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail(TEST_EMAIL);
        registerDto.setPassword(TEST_PASSWORD);
        registerDto.setUsername(TEST_USERNAME);

        // when
        HttpRequest request = HttpRequest.POST(REGISTER_PATH, registerDto);

        HttpResponse<Account> rsp =
                client.toBlocking().exchange(request, Account.class);

        Account accountResponse = rsp.body();

        Assertions.assertNotNull(accountResponse);
        Assertions.assertEquals(TEST_USERNAME, accountResponse.getUsername());
        Assertions.assertEquals(TEST_EMAIL, accountResponse.getEmail());

        // verify database entry
        Assertions.assertEquals(
                TEST_EMAIL,
                dslContext.selectFrom(USERS).where(USERS.USERNAME.equal(TEST_USERNAME))
                        .fetchAnyInto(com.org.mmo_server.repository.model.tables.pojos.Users.class)
                .getEmail()
        );

    }

}