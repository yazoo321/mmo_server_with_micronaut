package server.socket.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.websocket.WebSocketClient;
import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.annotation.OnMessage;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import server.common.dto.Motion;
import server.monster.server_integration.model.Monster;
import server.motion.dto.MotionResult;
import server.motion.dto.PlayerMotion;
import server.motion.model.MotionMessage;
import server.motion.service.PlayerMotionService;
import server.motion.socket.v2.controller.MotionSocketV2Test;
import server.socket.model.MessageType;
import server.socket.model.SocketMessage;
import server.socket.model.SocketResponseType;
import server.util.PlayerMotionUtil;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "kafka.enabled", value = "true")
@Property(name = "spec.name", value = "PlayerMotionSocketTest")
public class CommunicationSocketTest {
    @Inject BeanContext beanContext;

    @Inject EmbeddedServer embeddedServer;

    @Inject PlayerMotionService playerMotionService;

    @Inject PlayerMotionUtil playerMotionUtil;

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());
    private final ObjectReader objectReader = objectMapper.reader();

    private static final String MAP_1 = "map1";

    private final String CHARACTER_1 = "character1";
    private final String CHARACTER_2 = "character2";
    private final String CHARACTER_3 = "character3";

    private final String MOB_INSTANCE_ID_1 = "9b50e6c6-84d0-467f-b455-6b9c125f9105";
    private final String MOB_INSTANCE_ID_2 = "9b50e6c6-84d0-467f-b455-6b9c125f9106";
    private final String MOB_INSTANCE_ID_3 = "9b50e6c6-84d0-467f-b455-6b9c125f9107";

    private final String MOB_SERVER_NAME = "UE_SERVER_MAP_1";

    private final int TIMEOUT = 10;

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

        Thread.sleep(1000);

        List<String> messages1 = playerClient1.getMessagesChronologically();
        List<String> messages2 = playerClient2.getMessagesChronologically();

        Assertions.assertThat(messages1).isNotEmpty();
        Assertions.assertThat(messages2).isNotEmpty();

//        PlayerMotion expectedPlayerMotion = new PlayerMotion();
//        expectedPlayerMotion.setPlayerName(CHARACTER_1);
//        expectedPlayerMotion.setIsOnline(true);
//        expectedPlayerMotion.setMotion(motionWithinRange);
//
//        playerClient1.send(playerMotionWithinRange);
//
//        // player client 2 will receive this update
//        await().pollDelay(300, TimeUnit.MILLISECONDS)
//                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
//                .until(
//                        () ->
//                                playerMotionMatches(
//                                        getMotionResult(playerClient2), expectedPlayerMotion));
//
//        // now player 2 will move and player 1 will see this
//        expectedPlayerMotion.setPlayerName(CHARACTER_2);
//        playerClient2.send(playerMotionWithinRange);
//
//        await().pollDelay(300, TimeUnit.MILLISECONDS)
//                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
//                .until(
//                        () ->
//                                playerMotionMatches(
//                                        getMotionResult(playerClient1), expectedPlayerMotion));
//
//        // even if player 3 moves, this is not registered by player 1/2
//        playerClient3.send(playerMotionOutOfRange);
//        // TODO: static sleep not ideal
//        Thread.sleep(200);
//
//        PlayerMotion player3Motion = new PlayerMotion();
//        expectedPlayerMotion.setPlayerName(CHARACTER_3);
//        expectedPlayerMotion.setIsOnline(true);
//        expectedPlayerMotion.setMotion(motionOutOfRange);
//
//        Assertions.assertThat(getMotionResult(playerClient1).getPlayerMotion())
//                .usingRecursiveComparison()
//                .isNotEqualTo(player3Motion);

        playerClient1.close();
        playerClient2.close();
        playerClient3.close();
    }

//    @Test
//    public void testPlayerCanGetUpdatesOfNearbyMobs() throws Exception {
//        playerMotionService.initializePlayerMotion(CHARACTER_1).blockingGet();
//        playerMotionService.initializePlayerMotion(CHARACTER_2).blockingGet();
//
//        CommunicationSocketTest.TestWebSocketClient playerClient1 =
//                createWebSocketClient(embeddedServer.getPort(), MAP_1, CHARACTER_1);
//        CommunicationSocketTest.TestWebSocketClient playerClient2 =
//                createWebSocketClient(embeddedServer.getPort(), MAP_1, CHARACTER_2);
//        CommunicationSocketTest.TestWebSocketClient mobServerClient =
//                createWebSocketClient(embeddedServer.getPort(), MAP_1, MOB_SERVER_NAME);
//
//        Motion motionWithinRange = createBaseMotion();
//        Motion motionOutOfRange = createBaseMotion();
//        motionOutOfRange.setX(10_000);
//        motionOutOfRange.setY(10_000);
//
//        MotionMessage playerMotionWithinRange = new MotionMessage(motionWithinRange, true, null);
//        MotionMessage playerMotionOutOfRange = new MotionMessage(motionOutOfRange, true, null);
//
//        // initialize player 1 motion, which will see mob 1 and 2
//        playerClient1.send(playerMotionWithinRange);
//        // initialize player 2 motion, which will see mob 3
//        playerClient2.send(playerMotionOutOfRange);
//
//        // update set to 'false' to force create the mobs initially
//        MotionMessage mobMotionWithinRange1 =
//                new MotionMessage(motionWithinRange, false, MOB_INSTANCE_ID_1);
//        MotionMessage mobMotionWithinRange2 =
//                new MotionMessage(motionWithinRange, false, MOB_INSTANCE_ID_2);
//        MotionMessage mobMotionOutOfRange =
//                new MotionMessage(motionOutOfRange, false, MOB_INSTANCE_ID_3);
//
//        // mobs moving/initializing
//        mobServerClient.send(mobMotionWithinRange1);
//        mobServerClient.send(mobMotionWithinRange2);
//        mobServerClient.send(mobMotionOutOfRange);
//
//        Thread.sleep(1000); // let the server sync up again
//
//        // set to 'update' now to update instead of create
//        mobMotionWithinRange1.setUpdate(true);
//        mobMotionWithinRange2.setUpdate(true);
//        mobMotionOutOfRange.setUpdate(true);
//
//        // mob 1 will make a motion and player 1 and 2 should get this info
//        mobServerClient.send(mobMotionWithinRange1);
//
//        Monster expectedMobUpdate = new Monster();
//        expectedMobUpdate.setMobInstanceId(MOB_INSTANCE_ID_1);
//        expectedMobUpdate.setMotion(motionWithinRange);
//
//        Thread.sleep(200);
//
//        await().pollDelay(300, TimeUnit.MILLISECONDS)
//                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
//                .until(() -> mobMotionMatches(getMotionResult(playerClient1), expectedMobUpdate));
//
//        // mob 2 will make a motion and player 1 should get this info
//        mobServerClient.send(mobMotionWithinRange2);
//        expectedMobUpdate.setMobInstanceId(MOB_INSTANCE_ID_2);
//
//        await().pollDelay(300, TimeUnit.MILLISECONDS)
//                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
//                .until(() -> mobMotionMatches(getMotionResult(playerClient1), expectedMobUpdate));
//
//        // mob 3 will make a motion but player 1 will not see it.
//
//        mobServerClient.send(mobMotionOutOfRange);
//        expectedMobUpdate.setMobInstanceId(MOB_INSTANCE_ID_3);
//        expectedMobUpdate.getMotion().setX(10_000);
//        expectedMobUpdate.getMotion().setY(10_000);
//
//        Thread.sleep(200);
//
//        Assertions.assertThat(getMotionResult(playerClient1).getMonster())
//                .usingRecursiveComparison()
//                .isNotEqualTo(expectedMobUpdate);
//
//        //      however, player 2 is near this mob, so player 2 will see this update.
//        await().pollDelay(300, TimeUnit.MILLISECONDS)
//                .timeout(Duration.of(TIMEOUT, ChronoUnit.SECONDS))
//                .until(() -> mobMotionMatches(getMotionResult(playerClient2), expectedMobUpdate));
//
//        playerClient1.close();
//        playerClient2.close();
//        mobServerClient.close();
//    }

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
        playerMotion.setPlayerName(characterName);
        playerMessageWithinRange.setUpdateType(MessageType.PLAYER_MOTION.getType());

        return playerMessageWithinRange;
    }

    private MotionResult getMotionResult(CommunicationSocketTest.TestWebSocketClient client) {
        try {
            return objectReader.readValue(client.getLatestMessage(), MotionResult.class);
        } catch (Exception e) {
            return new MotionResult();
        }
    }

    private boolean playerMotionMatches(
            MotionResult motionResult, PlayerMotion expectedPlayerMotion) {
        if (motionResult.getPlayerMotion() == null) {
            return false;
        }
        PlayerMotion motion = motionResult.getPlayerMotion();

        // match all except updated_at
        return motion.getMotion().equals(expectedPlayerMotion.getMotion())
                && motion.getPlayerName().equals(expectedPlayerMotion.getPlayerName())
                && motion.getIsOnline().equals(expectedPlayerMotion.getIsOnline());
    }

    private boolean mobMotionMatches(MotionResult motionResult, Monster expectedMob) {
        if (motionResult.getMonster() == null) {
            return false;
        }

        Monster mob = motionResult.getMonster();

        return mob.getMobInstanceId().equals(expectedMob.getMobInstanceId())
                && mob.getMotion().equals(expectedMob.getMotion());
    }

}
