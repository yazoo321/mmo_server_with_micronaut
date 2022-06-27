package server.security;

import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
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

import javax.inject.Inject;
import java.text.ParseException;
import java.time.LocalDateTime;

import static com.org.mmo_server.repository.model.tables.UserRoles.*;
import static com.org.mmo_server.repository.model.tables.Users.USERS;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
// Disabled auth in application.yml, therefore disable tests
@Disabled
public class JwtAuthenticationTest {

    @Inject
    EmbeddedServer embeddedServer;
    RxHttpClient client;

    @Inject
    DSLContext dslContext;

    @Inject
    BCryptPasswordEncoderService bCryptPasswordEncoderService;

    private static final String GET_USER_PATH = "/account/get-user?username=username";
    private static final String VALID_USER = "username";
    private static final String VALID_PW = "password";
    private static final String VALID_EMAIL = "email";

    @BeforeAll
    void setupDatabase() {
        cleanDb();
        seedDb();

        client  = embeddedServer.getApplicationContext()
                .createBean(RxHttpClient.class, embeddedServer.getURL());
    }

    @AfterAll
    void cleanUp() {
        cleanDb();

        embeddedServer.stop();
        client.stop();
    }

    private void cleanDb() {
        dslContext.deleteFrom(USER_ROLES).where(USER_ROLES.USERNAME.equal(VALID_USER)).execute();
        dslContext.deleteFrom(USERS).where(USERS.USERNAME.equal(VALID_USER)).execute();
    }

    private void seedDb() {
        String encryptedPw = bCryptPasswordEncoderService.encode(VALID_PW);
        LocalDateTime now = LocalDateTime.now();
        dslContext.insertInto(USERS)
                .columns(USERS.USERNAME, USERS.EMAIL, USERS.PASSWORD,
                        USERS.ENABLED, USERS.CREATED_AT, USERS.UPDATED_AT,
                        USERS.LAST_LOGGED_IN_AT)
                .values(VALID_USER, VALID_EMAIL, encryptedPw,
                        true, now, now, now).execute();

        dslContext.insertInto(USER_ROLES)
                .columns(USER_ROLES.USERNAME, USER_ROLES.ROLE)
                .values(VALID_USER, "ROLE_USER").execute();
    }

    @Test
    void testProtectedEndpointThrowsOnUnauthorized() {
        // when
        Flowable<Account> response = client.retrieve(
                HttpRequest.GET(GET_USER_PATH), Account.class
        );

        // then
        Assertions.assertThrows(HttpClientResponseException.class, response::blockingFirst);
    }

    @Test
    void testProtectedEndpointReturnsExpectedWhenAuthorizedWithBasicAuth() {
        // when
        Flowable<Account> response = client.retrieve(
                HttpRequest.GET(GET_USER_PATH).basicAuth(VALID_USER, VALID_PW), Account.class);

        // then
        Assertions.assertEquals(VALID_USER, response.blockingFirst().getUsername());
        Assertions.assertEquals(VALID_EMAIL, response.blockingFirst().getEmail());
    }

    @Test
    void testLoginRequestWorkingAsExpected() throws ParseException {
        // given
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(VALID_USER, VALID_PW);

        // when
        HttpRequest request = HttpRequest.POST("/login", creds);
        HttpResponse<BearerAccessRefreshToken> rsp =
                client.toBlocking().exchange(request, BearerAccessRefreshToken.class);

        // then
        Assertions.assertEquals(HttpStatus.OK, rsp.getStatus());
        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body();
        Assertions.assertEquals(VALID_USER, bearerAccessRefreshToken.getUsername());
        Assertions.assertNotNull(bearerAccessRefreshToken.getAccessToken());
        Assertions.assertTrue(JWTParser.parse(bearerAccessRefreshToken.getAccessToken()) instanceof SignedJWT);
    }

    @Test
    void testLoginAccessTokenCanBeUsedOnSecuredEndpoint() {
        // given
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(VALID_USER, VALID_PW);

        // when
        HttpRequest request = HttpRequest.POST("/login", creds);
        HttpResponse<BearerAccessRefreshToken> rsp =
                client.toBlocking().exchange(request, BearerAccessRefreshToken.class);

        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body();
        String accessToken = bearerAccessRefreshToken.getAccessToken();

        Flowable<Account> response = client.retrieve(
                HttpRequest.GET(GET_USER_PATH).bearerAuth(accessToken), Account.class);

        Assertions.assertEquals(VALID_USER, response.blockingFirst().getUsername());
        Assertions.assertEquals(VALID_EMAIL, response.blockingFirst().getEmail());
    }

}
