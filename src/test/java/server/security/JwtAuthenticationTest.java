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
public class JwtAuthenticationTest {

    @Inject
    EmbeddedServer embeddedServer;
    RxHttpClient client;

    @Inject
    DSLContext dslContext;

    private final static String GET_USER_PATH = "/account/get-user?username=username";
    private static final String VALID_USER = "username";
    private static final String VALID_PW = "password";

    @BeforeAll
    void setupDatabase() {
        LocalDateTime now = LocalDateTime.now();
        dslContext.insertInto(USERS)
                .columns(USERS.USERNAME, USERS.EMAIL, USERS.PASSWORD,
                        USERS.ENABLED, USERS.CREATED_AT, USERS.UPDATED_AT,
                        USERS.LAST_LOGGED_IN_AT)
                .values("username", "email", "password",
                        true, now, now, now);

        dslContext.insertInto(USER_ROLES)
                .columns(USER_ROLES.USERNAME, USER_ROLES.ROLE)
                .values("username", "role");

        client  = embeddedServer.getApplicationContext()
                .createBean(RxHttpClient.class, embeddedServer.getURL());
    }

    @AfterAll
    void cleanUp() {
        embeddedServer.stop();
        client.stop();
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
        Assertions.assertEquals("username", response.blockingFirst().getUsername());
        Assertions.assertEquals("email", response.blockingFirst().getEmail());
    }

    @Test
    void testLoginRequestWorkingAsExpected() throws ParseException {
        // given
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("username", "password");

        // when
        HttpRequest request = HttpRequest.POST("/login", creds);
        HttpResponse<BearerAccessRefreshToken> rsp =
                client.toBlocking().exchange(request, BearerAccessRefreshToken.class);

        // then
        Assertions.assertEquals(HttpStatus.OK, rsp.getStatus());
        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body();
        Assertions.assertEquals("username", bearerAccessRefreshToken.getUsername());
        Assertions.assertNotNull(bearerAccessRefreshToken.getAccessToken());
        Assertions.assertTrue(JWTParser.parse(bearerAccessRefreshToken.getAccessToken()) instanceof SignedJWT);
    }

    @Test
    void testLoginAccessTokenCanBeUsedOnSecuredEndpoint() {
        // given
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("username", "password");

        // when
        HttpRequest request = HttpRequest.POST("/login", creds);
        HttpResponse<BearerAccessRefreshToken> rsp =
                client.toBlocking().exchange(request, BearerAccessRefreshToken.class);

        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body();
        String accessToken = bearerAccessRefreshToken.getAccessToken();

        Flowable<Account> response = client.retrieve(
                HttpRequest.GET(GET_USER_PATH).bearerAuth(accessToken), Account.class);

        Assertions.assertEquals("username", response.blockingFirst().getUsername());
        Assertions.assertEquals("email", response.blockingFirst().getEmail());
    }

}
