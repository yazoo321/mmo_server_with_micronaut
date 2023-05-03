package server.socket.v1;

import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.websocket.WebSocketClient;
import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.annotation.OnMessage;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import server.common.dto.Motion;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.motion.service.PlayerMotionService;
import server.socket.model.*;
import server.util.PlayerMotionUtil;

@MicronautTest(environments = "kafka")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "spec.name", value = "PlayerMotionSocketTest")
public class CommunicationSocketTest {
    @Inject BeanContext beanContext;

    @Inject EmbeddedServer embeddedServer;

    @Inject PlayerMotionService playerMotionService;

    @Inject PlayerMotionUtil playerMotionUtil;

    @Inject
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private final ObjectReader objectReader = objectMapper.reader();

    private static final String MAP_1 = "map1";

    private final String CHARACTER_1 = "character1";
    private final String CHARACTER_2 = "character2";
    private final String CHARACTER_3 = "character3";

    private final String MOB_INSTANCE_ID_1 = "9b50e6c6-84d0-467f-b455-6b9c125f9105";
    private final String MOB_INSTANCE_ID_2 = "9b50e6c6-84d0-467f-b455-6b9c125f9106";
    private final String MOB_INSTANCE_ID_3 = "9b50e6c6-84d0-467f-b455-6b9c125f9107";

    private final String MOB_SERVER_NAME = "UE_SERVER_MAP_1";

    private final int TIMEOUT = 50;

    @BeforeEach
    void setup() {
        cleanup();
    }

    @AfterAll
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        playerMotionUtil.deleteAllPlayerMotionData();
        playerMotionUtil.deleteAllMobInstanceData();
    }

    @ClientWebSocket
    abstract static class TestWebSocketClient implements AutoCloseable {

        private final Deque<String> messageHistory = new ConcurrentLinkedDeque<>();

        public String getLatestMessage() {
            return messageHistory.peekLast();
        }

        public List<String> getMessagesChronologically() {
            return new ArrayList<>(messageHistory);
        }

        @OnMessage
        void onMessage(String message) {
            messageHistory.add(message);
        }

        abstract void send(SocketMessage message);
    }

    private CommunicationSocketTest.TestWebSocketClient createWebSocketClient(int port) {
        WebSocketClient webSocketClient = beanContext.getBean(WebSocketClient.class);
        URI uri =
                UriBuilder.of("ws://localhost")
                        .port(port)
                        .path("v1")
                        .path("communication-socket")
                        .build();

        Publisher<CommunicationSocketTest.TestWebSocketClient> client =
                webSocketClient.connect(CommunicationSocketTest.TestWebSocketClient.class, uri);
        return Flux.from(client).blockFirst();
    }

    private Motion createBaseMotion() {
        return Motion.builder()
                .x(100)
                .y(110)
                .z(120)
                .vx(200)
                .vy(210)
                .vz(220)
                .pitch(300)
                .roll(310)
                .yaw(320)
                .map(MAP_1)
                .build();
    }

    @Test
    void testBasicMotionUpdateBetween2Players() throws Exception {
        playerMotionService.initializePlayerMotion(CHARACTER_1).blockingGet();
        playerMotionService.initializePlayerMotion(CHARACTER_2).blockingGet();
        playerMotionService.initializePlayerMotion(CHARACTER_3).blockingGet();

        CommunicationSocketTest.TestWebSocketClient playerClient1 =
                createWebSocketClient(embeddedServer.getPort());
        CommunicationSocketTest.TestWebSocketClient playerClient2 =
                createWebSocketClient(embeddedServer.getPort());
        CommunicationSocketTest.TestWebSocketClient playerClient3 =
                createWebSocketClient(embeddedServer.getPort());

        SocketMessage char1WithinRange = createMessageForMotionWithinRange(CHARACTER_1);
        SocketMessage char2WithinRange = createMessageForMotionWithinRange(CHARACTER_2);
        SocketMessage char3OutOfRange = createMessageForMotionOutOfRange(CHARACTER_3);

        // players moving/initializing
        playerClient1.send(char1WithinRange);
        playerClient2.send(char2WithinRange);
        playerClient3.send(char3OutOfRange);

        // let server sync up
        // TODO: Make this parameterized through application-test.yml
        Thread.sleep(1000);

        playerClient1.send(char1WithinRange);
        playerClient2.send(char2WithinRange);

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> playerClient1.getMessagesChronologically().size() == 2);

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
                .until(() -> playerClient2.getMessagesChronologically().size() == 2);

        SocketResponse expectedMotionResponseForClient1 =
                SocketResponse.builder()
                        .messageType(SocketResponseType.NEW_PLAYER_MOTION.getType())
                        .playerMotion(Map.of(CHARACTER_2, char2WithinRange.getPlayerMotion()))
                        .build();

        SocketResponse expectedMotionResponseForClient2 =
                SocketResponse.builder()
                        .messageType(SocketResponseType.NEW_PLAYER_MOTION.getType())
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
                                                        SocketResponseType.NEW_PLAYER_MOTION
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
                                                        SocketResponseType.NEW_PLAYER_MOTION
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

        CommunicationSocketTest.TestWebSocketClient playerClient1 =
                createWebSocketClient(embeddedServer.getPort());
        CommunicationSocketTest.TestWebSocketClient playerClient2 =
                createWebSocketClient(embeddedServer.getPort());
        CommunicationSocketTest.TestWebSocketClient mobServerClient =
                createWebSocketClient(embeddedServer.getPort());

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

        // TODO: This occasionally flakes
        Assertions.assertThat(mobClientResponses.size()).isEqualTo(2);
        Assertions.assertThat(
                        mobClientResponses.stream().map(SocketResponse::getMessageType).toList())
                .containsExactlyInAnyOrder(
                        SocketResponseType.NEW_PLAYER_MOTION.getType(),
                        SocketResponseType.NEW_PLAYER_APPEARANCE.getType());

        playerClient1.close();
        playerClient2.close();
        mobServerClient.close();
    }

    private SocketMessage createMessageForMotionOutOfRange(String characterName) {
        SocketMessage socketMsg = createMessageForMotionWithinRange(characterName);
        Motion motion = socketMsg.getPlayerMotion().getMotion();

        motion.setX(10_000);
        motion.setY(10_000);

        return socketMsg;
    }

    private SocketMessage createMessageForMotionWithinRange(String characterName) {
        Motion motionWithinRange = createBaseMotion();
        PlayerMotion playerMotion = new PlayerMotion();
        playerMotion.setMotion(motionWithinRange);
        playerMotion.setPlayerName(characterName);

        SocketMessage playerMessageWithinRange = new SocketMessage();
        playerMessageWithinRange.setPlayerMotion(playerMotion);
        playerMessageWithinRange.setPlayerName(characterName);
        playerMessageWithinRange.setUpdateType(MessageType.PLAYER_MOTION.getType());

        return playerMessageWithinRange;
    }

    private SocketMessage createMobMessageForMotionWithinRange(String mobInstanceId) {
        Motion motionWithinRange = createBaseMotion();

        Monster mob = new Monster();
        mob.setMobInstanceId(mobInstanceId);
        mob.setMobId(mobInstanceId);
        mob.setMotion(motionWithinRange);

        SocketMessage msg = new SocketMessage();
        msg.setMobId(mobInstanceId);
        msg.setMobInstanceId(mobInstanceId);
        msg.setMonster(mob);
        msg.setServerName("SERVER_1");

        return msg;
    }

    private SocketMessage createMobMessageForMotionOutOfRange(String mobInstanceId) {
        SocketMessage msg = createMobMessageForMotionWithinRange(mobInstanceId);

        msg.getMonster().getMotion().setX(10_000);
        msg.getMonster().getMotion().setY(10_000);

        return msg;
    }

    private List<SocketResponse> getSocketResponse(
            CommunicationSocketTest.TestWebSocketClient client) {
        List<SocketResponse> allResponses = new ArrayList<>();
        client.getMessagesChronologically()
                .forEach(
                        message -> {
                            try {
                                allResponses.add(
                                        objectReader.readValue(message, SocketResponse.class));
                            } catch (IOException e) {
                                // ignore
                            }
                        });

        return allResponses;
    }
}
