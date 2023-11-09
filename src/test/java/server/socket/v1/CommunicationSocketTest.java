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
        TestWebSocketClient playerClient1 = initiateSocketAsPlayer(CHARACTER_1);
        TestWebSocketClient playerClient2 = initiateSocketAsPlayer(CHARACTER_2);
        TestWebSocketClient playerClient3 = initiateSocketAsPlayer(CHARACTER_3);

        SocketMessage char1WithinRange = createMessageForMotionWithinRange(CHARACTER_1);
        SocketMessage char2WithinRange = createMessageForMotionWithinRange(CHARACTER_2);
        SocketMessage char3OutOfRange = createMessageForMotionOutOfRange(CHARACTER_3);

        // players moving/initializing
        playerClient1.send(char1WithinRange);
        playerClient2.send(char2WithinRange);
        playerClient3.send(char3OutOfRange);


        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> {
                    playerClient2.send(char2WithinRange);

                    return playerClient1.getMessagesChronologically().size() > 1;
                });

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> {
                    playerClient1.send(char1WithinRange);
                    return playerClient2.getMessagesChronologically().size() > 1;
                });

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

        Assertions.assertThat(newMotionPlayer1.get(newMotionPlayer1.size()-1).getPlayerMotion().get(CHARACTER_2))
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

        Assertions.assertThat(newMotionPlayer2.get(newMotionPlayer2.size()-1).getPlayerMotion().get(CHARACTER_1))
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
        TestWebSocketClient playerClient1 = initiateSocketAsPlayer(CHARACTER_1);
        TestWebSocketClient playerClient2 = initiateSocketAsPlayer(CHARACTER_2);

        TestWebSocketClient mobServerClient1 = initiateSocketAsServer("SERVER_NAME_1");
        TestWebSocketClient mobServerClient2 = initiateSocketAsServer("SERVER_NAME_2");

        SocketMessage char1WithinRange = createMessageForMotionWithinRange(CHARACTER_1);
        SocketMessage char2OutOfRange = createMessageForMotionOutOfRange(CHARACTER_2);

        SocketMessage mobWithinRange = createMobMessageForMotionWithinRange(MOB_INSTANCE_ID_1);
        SocketMessage mobOutOfRange = createMobMessageForMotionOutOfRange(MOB_INSTANCE_ID_2);

        // first, create the mobs
        mobWithinRange.setUpdateType(MessageType.CREATE_MOB.getType());
        mobOutOfRange.setUpdateType(MessageType.CREATE_MOB.getType());

        mobServerClient1.send(mobWithinRange);
        mobServerClient2.send(mobOutOfRange);

        playerClient1.send(char1WithinRange);
        playerClient2.send(char2OutOfRange);

        // now we can make motion which should be tracked
        mobWithinRange.setUpdateType(MessageType.MOB_MOTION.getType());
        mobOutOfRange.setUpdateType(MessageType.MOB_MOTION.getType());

        await().pollDelay(500, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> {
                    mobServerClient1.send(mobWithinRange);
                    mobServerClient2.send(mobOutOfRange);
                    return !playerClient1.getMessagesChronologically().isEmpty();
                });;

        await().pollDelay(500, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> !playerClient2.getMessagesChronologically().isEmpty());

        await().pollDelay(500, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> {
                    playerClient1.send(char1WithinRange);
                    playerClient2.send(char2OutOfRange);
                    return !mobServerClient1.getMessagesChronologically().isEmpty();
                });

        List<SocketResponse> client1Responses = getSocketResponse(playerClient1);
        List<SocketResponse> client2Responses = getSocketResponse(playerClient2);
        List<SocketResponse> mobClient1Responses = getSocketResponse(mobServerClient1);
        List<SocketResponse> mobClient2Responses = getSocketResponse(mobServerClient2);

        Assertions.assertThat(client1Responses.get(client1Responses.size()-1).getMonsters())
                .usingRecursiveComparison()
                .ignoringFields("9b50e6c6-84d0-467f-b455-6b9c125f9105.updatedAt")
                .isEqualTo(
                        Map.of(
                                mobWithinRange.getMonster().getMobInstanceId(),
                                mobWithinRange.getMonster()));

        Assertions.assertThat(client2Responses.get(client2Responses.size()-1).getMonsters())
                .usingRecursiveComparison()
                .ignoringFields("9b50e6c6-84d0-467f-b455-6b9c125f9106.updatedAt")
                .isEqualTo(
                        Map.of(
                                mobOutOfRange.getMonster().getMobInstanceId(),
                                mobOutOfRange.getMonster()));

        Assertions.assertThat(
                        mobClient1Responses.stream().map(SocketResponse::getMessageType).toList())
                .contains(
                        SocketResponseType.PLAYER_MOTION_UPDATE.getType(),
                        SocketResponseType.PLAYER_APPEARANCE.getType());

        Assertions.assertThat(
                        mobClient2Responses.stream().map(SocketResponse::getMessageType).toList())
                .contains(
                        SocketResponseType.PLAYER_MOTION_UPDATE.getType(),
                        SocketResponseType.PLAYER_APPEARANCE.getType());
        playerClient1.close();
        playerClient2.close();
        mobServerClient1.close();
        mobServerClient2.close();
    }
}
