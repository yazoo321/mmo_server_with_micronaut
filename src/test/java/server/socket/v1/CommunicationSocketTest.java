package server.socket.v1;

import static org.awaitility.Awaitility.await;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import server.socket.model.*;
import server.util.websocket.TestWebSocketClient;

@MicronautTest(environments = "kafka")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "spec.name", value = "PlayerMotionSocketTest")
public class CommunicationSocketTest extends CommunicationSocketItemsTest {

    @Test
    void testBasicMotionUpdateBetween2Players() throws Exception {
        playerMotionService.initializePlayerMotion(CHARACTER_1).blockingGet();
        playerMotionService.initializePlayerMotion(CHARACTER_2).blockingGet();
        playerMotionService.initializePlayerMotion(CHARACTER_3).blockingGet();

        TestWebSocketClient playerClient1 = createWebSocketClient(embeddedServer.getPort());
        TestWebSocketClient playerClient2 = createWebSocketClient(embeddedServer.getPort());
        TestWebSocketClient playerClient3 = createWebSocketClient(embeddedServer.getPort());

        SocketMessage char1WithinRange = createMessageForMotionWithinRange(CHARACTER_1);
        SocketMessage char2WithinRange = createMessageForMotionWithinRange(CHARACTER_2);
        SocketMessage char3OutOfRange = createMessageForMotionOutOfRange(CHARACTER_3);

        // players moving/initializing
        playerClient1.send(char1WithinRange);
        playerClient2.send(char2WithinRange);
        playerClient3.send(char3OutOfRange);

        // let server sync up
        // TODO: Make this parameterized
        Thread.sleep(1000);

        playerClient1.send(char1WithinRange);
        playerClient2.send(char2WithinRange);

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(60, ChronoUnit.SECONDS))
                .until(() ->
                        playerClient1.getMessagesChronologically().size() == 2);

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> playerClient2.getMessagesChronologically().size() == 2);

        SocketResponse expectedMotionResponseForClient1 =
                SocketResponse.builder()
                        .messageType(SocketResponseType.PLAYER_MOTION_UPDATE.getType())
                        .playerMotion(Map.of(CHARACTER_2, char2WithinRange.getPlayerMotion()))
                        .build();

        SocketResponse expectedMotionResponseForClient2 =
                SocketResponse.builder()
                        .messageType(SocketResponseType.PLAYER_MOTION_UPDATE.getType())
                        .playerMotion(Map.of(CHARACTER_1, char1WithinRange.getPlayerMotion()))
                        .build();

        List<SocketResponse> responsesPlayer1 = getSocketResponse(playerClient1);
        List<SocketResponse> responsesPlayer2 = getSocketResponse(playerClient2);
        List<SocketResponse> responsesPlayer3 = getSocketResponse(playerClient3);

        List<SocketResponse> newMotionPlayer1 =
                responsesPlayer1.stream()
                        .filter(
                                r ->
                                        r.getMessageType()
                                                .equals(
                                                        SocketResponseType.PLAYER_MOTION_UPDATE
                                                                .getType()))
                        .toList();

        Assertions.assertThat(newMotionPlayer1.size()).isEqualTo(1);
        Assertions.assertThat(newMotionPlayer1.get(0).getPlayerMotion().get(CHARACTER_2))
                .usingRecursiveComparison()
                .ignoringFields("isOnline", "updatedAt")
                .isEqualTo(expectedMotionResponseForClient1.getPlayerMotion().get(CHARACTER_2));

        List<SocketResponse> newMotionPlayer2 =
                responsesPlayer2.stream()
                        .filter(
                                r ->
                                        r.getMessageType()
                                                .equals(
                                                        SocketResponseType.PLAYER_MOTION_UPDATE
                                                                .getType()))
                        .toList();

        Assertions.assertThat(newMotionPlayer2.size()).isEqualTo(1);
        Assertions.assertThat(newMotionPlayer2.get(0).getPlayerMotion().get(CHARACTER_1))
                .usingRecursiveComparison()
                .ignoringFields("isOnline", "updatedAt")
                .isEqualTo(expectedMotionResponseForClient2.getPlayerMotion().get(CHARACTER_1));

        Assertions.assertThat(responsesPlayer3).isEmpty();

        playerClient1.close();
        playerClient2.close();
        playerClient3.close();
    }

    @Test
    public void testPlayerCanGetUpdatesOfNearbyMobs() throws Exception {
        playerMotionService.initializePlayerMotion(CHARACTER_1).blockingGet();
        playerMotionService.initializePlayerMotion(CHARACTER_2).blockingGet();

        TestWebSocketClient playerClient1 = createWebSocketClient(embeddedServer.getPort());
        TestWebSocketClient playerClient2 = createWebSocketClient(embeddedServer.getPort());
        TestWebSocketClient mobServerClient = createWebSocketClient(embeddedServer.getPort());

        SocketMessage char1WithinRange = createMessageForMotionWithinRange(CHARACTER_1);
        SocketMessage char2OutOfRange = createMessageForMotionOutOfRange(CHARACTER_2);

        SocketMessage mobWithinRange = createMobMessageForMotionWithinRange(MOB_INSTANCE_ID_1);
        SocketMessage mobOutOfRange = createMobMessageForMotionOutOfRange(MOB_INSTANCE_ID_2);

        // first, create the mobs
        mobWithinRange.setUpdateType(MessageType.CREATE_MOB.getType());
        mobOutOfRange.setUpdateType(MessageType.CREATE_MOB.getType());

        mobServerClient.send(mobWithinRange);
        mobServerClient.send(mobOutOfRange);

        playerClient1.send(char1WithinRange);
        playerClient2.send(char2OutOfRange);

        // first we need to make sure the player clients are started and synchronised.
        Thread.sleep(1000);

        playerClient1.send(char1WithinRange);
        playerClient2.send(char2OutOfRange);

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> !playerClient1.getMessagesChronologically().isEmpty());

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> !playerClient2.getMessagesChronologically().isEmpty());

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> !mobServerClient.getMessagesChronologically().isEmpty());

        List<SocketResponse> client1Responses = getSocketResponse(playerClient1);
        List<SocketResponse> client2Responses = getSocketResponse(playerClient2);
        List<SocketResponse> mobClientResponses = getSocketResponse(mobServerClient);

        Assertions.assertThat(client1Responses.size()).isEqualTo(1);
        Assertions.assertThat(client1Responses.get(0).getMonsters())
                .usingRecursiveComparison()
                .ignoringFields("9b50e6c6-84d0-467f-b455-6b9c125f9105.updatedAt")
                .isEqualTo(
                        Map.of(
                                mobWithinRange.getMonster().getMobInstanceId(),
                                mobWithinRange.getMonster()));

        Assertions.assertThat(client2Responses.size()).isEqualTo(1);
        Assertions.assertThat(client2Responses.get(0).getMonsters())
                .usingRecursiveComparison()
                .ignoringFields("9b50e6c6-84d0-467f-b455-6b9c125f9106.updatedAt")
                .isEqualTo(
                        Map.of(
                                mobOutOfRange.getMonster().getMobInstanceId(),
                                mobOutOfRange.getMonster()));

        Assertions.assertThat(mobClientResponses.size())
                .isLessThan(4); // we can get 2x motion updates and 1x appearance update

        Assertions.assertThat(
                        mobClientResponses.stream().map(SocketResponse::getMessageType).toList())
                .contains(
                        SocketResponseType.PLAYER_MOTION_UPDATE.getType(),
                        SocketResponseType.PLAYER_APPEARANCE.getType());

        playerClient1.close();
        playerClient2.close();
        mobServerClient.close();
    }
}
