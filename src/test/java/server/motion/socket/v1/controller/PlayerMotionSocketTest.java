package server.motion.socket.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.websocket.WebSocketClient;
import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.annotation.OnMessage;
import jakarta.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.junit.jupiter.api.BeforeEach;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import server.common.dto.Motion;
import server.motion.model.MotionMessage;
import server.motion.model.PlayerMotionList;
import server.motion.service.PlayerMotionService;
import server.util.PlayerMotionUtil;

@Property(name = "spec.name", value = "PlayerMotionSocketTest")
@MicronautTest
public class PlayerMotionSocketTest {

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

    @BeforeEach
    void setup() {
        playerMotionUtil.deleteAllPlayerMotionData();
    }

    @Requires(property = "spec.name", value = "PlayerMotionSocketTest")
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

        abstract void send(MotionMessage message);
    }

    private TestWebSocketClient createWebSocketClient(int port, String map, String actorId) {
        WebSocketClient webSocketClient = beanContext.getBean(WebSocketClient.class);
        URI uri =
                UriBuilder.of("ws://localhost")
                        .port(port)
                        .path("v1")
                        .path("player-motion")
                        .path("{map}")
                        .path("{actorId}")
                        .expand(CollectionUtils.mapOf("map", map, "actorId", actorId));
        Publisher<TestWebSocketClient> client =
                webSocketClient.connect(TestWebSocketClient.class, uri);
        // requires to install reactor
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

    //    @Test
    //    void testBasicMotionUpdateBetween2Players() throws Exception {
    //        playerMotionService.initializePlayerMotion(CHARACTER_1).blockingGet();
    //        playerMotionService.initializePlayerMotion(CHARACTER_2).blockingGet();
    //
    //        TestWebSocketClient client1 =
    //                createWebSocketClient(embeddedServer.getPort(), MAP_1, CHARACTER_1);
    //        TestWebSocketClient client2 =
    //                createWebSocketClient(embeddedServer.getPort(), MAP_1, CHARACTER_2);
    //
    //        String expectedMsgCharacter1 = "[character1] Joined map1!";
    //        String expectedMsgCharacter2 = "[character2] Joined map1!";
    //
    //        await().pollDelay(100, TimeUnit.MILLISECONDS)
    //                .timeout(Duration.of(1, ChronoUnit.SECONDS))
    //                .until(
    //                        () ->
    //                                Collections.singletonList(expectedMsgCharacter1)
    //                                        .equals(client1.getMessagesChronologically()));
    //
    //        await().pollDelay(100, TimeUnit.MILLISECONDS)
    //                .timeout(Duration.of(1, ChronoUnit.SECONDS))
    //                .until(
    //                        () ->
    //                                Collections.singletonList(expectedMsgCharacter2)
    //                                        .equals(client2.getMessagesChronologically()));
    //
    //        // Update motion on player 1
    //        Motion motion = createBaseMotion();
    //        MotionMessage playerMotionMessage = new MotionMessage(motion, true, null);
    //
    //        // prepare client 1 to send motion data
    //        client1.send(playerMotionMessage);
    //
    //        // expected response in client 2 (client1 motion)
    //        PlayerMotion expectedPlayerMotion = new PlayerMotion();
    //        expectedPlayerMotion.setActorId(CHARACTER_1);
    //        expectedPlayerMotion.setIsOnline(true);
    //        expectedPlayerMotion.setMotion(motion);
    //
    //        PlayerMotionList expectedPlayerMotionList =
    //                new PlayerMotionList(List.of(expectedPlayerMotion));
    //
    //        // client 1 will send motion and there's nothing around, so empty result returned
    //        await().pollDelay(300, TimeUnit.MILLISECONDS)
    //                .timeout(Duration.of(3, ChronoUnit.SECONDS))
    //                .until(() -> getPlayerMotionList(client1).getPlayerMotionList().isEmpty());
    //
    //        // client 2 will now make some motion
    //        client2.send(playerMotionMessage);
    //
    //        Thread.sleep(1000);
    //
    //        // client 2 will have motion of client 1 as its nearby
    //        await().pollDelay(300, TimeUnit.MILLISECONDS)
    //                .timeout(Duration.of(10, ChronoUnit.SECONDS))
    //                .until(
    //                        () ->
    //                                PlayerMotionUtil.playerMotionListEquals(
    //                                        getPlayerMotionList(client2),
    // expectedPlayerMotionList));
    //
    //        // if client 1 updates motion again, it will receive new updates from client 2
    //        client1.send(playerMotionMessage);
    //
    //        // client 1 should receive update from client 2
    //        PlayerMotion expectedPlayerMotion2 = new PlayerMotion();
    //        expectedPlayerMotion2.setActorId(CHARACTER_2);
    //        expectedPlayerMotion2.setIsOnline(true);
    //        expectedPlayerMotion2.setMotion(motion);
    //        PlayerMotionList expectedPlayerMotionList2 =
    //                new PlayerMotionList(List.of(expectedPlayerMotion2));
    //
    //        // expect client 1 to receive the update from client 2
    //        await().pollDelay(300, TimeUnit.MILLISECONDS)
    //                .timeout(Duration.of(3, ChronoUnit.SECONDS))
    //                .until(
    //                        () ->
    //                                PlayerMotionUtil.playerMotionListEquals(
    //                                        getPlayerMotionList(client1),
    // expectedPlayerMotionList2));
    //
    //        client1.close();
    //        client2.close();
    //    }

    private PlayerMotionList getPlayerMotionList(TestWebSocketClient client) {
        try {
            return objectReader.readValue(client.getLatestMessage(), PlayerMotionList.class);
        } catch (Exception e) {
            return new PlayerMotionList();
        }
    }
}
