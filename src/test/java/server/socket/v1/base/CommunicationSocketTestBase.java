package server.socket.v1.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micronaut.context.BeanContext;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.websocket.WebSocketClient;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import server.common.dto.Motion;
import server.items.helper.ItemTestHelper;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.motion.service.PlayerMotionService;
import server.socket.model.SocketMessage;
import server.socket.model.SocketResponse;
import server.socket.model.types.MessageType;
import server.util.PlayerMotionUtil;
import server.util.websocket.TestWebSocketClient;

@MicronautTest(environments = "kafka")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommunicationSocketTestBase {

    @Inject protected BeanContext beanContext;

    @Inject protected ItemTestHelper itemTestHelper;

    @Inject protected EmbeddedServer embeddedServer;

    @Inject protected PlayerMotionService playerMotionService;

    @Inject protected PlayerMotionUtil playerMotionUtil;

    @Inject
    protected final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    protected final ObjectReader objectReader = objectMapper.reader();

    protected static final String MAP_1 = "map1";

    protected final String CHARACTER_1 = "character1";
    protected final String CHARACTER_2 = "character2";
    protected final String CHARACTER_3 = "character3";

    protected final String MOB_INSTANCE_ID_1 = "9b50e6c6-84d0-467f-b455-6b9c125f9105";
    protected final String MOB_INSTANCE_ID_2 = "9b50e6c6-84d0-467f-b455-6b9c125f9106";
    protected final String MOB_INSTANCE_ID_3 = "9b50e6c6-84d0-467f-b455-6b9c125f9107";

    protected final String MOB_SERVER_NAME = "UE_SERVER_MAP_1";

    protected final int TIMEOUT = 5;

    @BeforeEach
    public void setup() {
        cleanup();
    }

    @AfterAll
    public void tearDown() {
        cleanup();
    }

    protected void cleanup() {
        playerMotionUtil.deleteAllPlayerMotionData();
        playerMotionUtil.deleteAllMobInstanceData();
        itemTestHelper.deleteAllItemData();
    }

    protected server.util.websocket.TestWebSocketClient createWebSocketClient(int port) {
        WebSocketClient webSocketClient = beanContext.getBean(WebSocketClient.class);
        URI uri =
                UriBuilder.of("ws://localhost")
                        .port(port)
                        .path("v1")
                        .path("communication-socket")
                        .build();

        Publisher<server.util.websocket.TestWebSocketClient> client =
                webSocketClient.connect(server.util.websocket.TestWebSocketClient.class, uri);
        return Flux.from(client).blockFirst();
    }

    protected TestWebSocketClient initiateSocketAsServer(String serverName) {
        TestWebSocketClient client = createWebSocketClient(embeddedServer.getPort());
        SocketMessage msg = new SocketMessage();
        msg.setUpdateType(MessageType.SET_SESSION_ID.getType());
        msg.setServerName(serverName);

        client.send(msg);

        return client;
    }

    protected void initiatePlayer(String actorId) {
        playerMotionService.initializePlayerMotion(actorId).blockingSubscribe();
        itemTestHelper.prepareInventory(actorId);
    }

    protected TestWebSocketClient initiateSocketAsPlayer(String actorId) {
        initiatePlayer(actorId);

        TestWebSocketClient playerClient = createWebSocketClient(embeddedServer.getPort());

        SocketMessage msg = new SocketMessage();
        msg.setUpdateType(MessageType.SET_SESSION_ID.getType());
        msg.setActorId(actorId);

        playerClient.send(msg);

        return playerClient;
    }

    protected Motion createBaseMotion() {
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

    protected SocketMessage createMessageForMotionOutOfRange(String actorId) {
        SocketMessage socketMsg = createMessageForMotionWithinRange(actorId);
        Motion motion = socketMsg.getPlayerMotion().getMotion();

        motion.setX(10_000);
        motion.setY(10_000);

        return socketMsg;
    }

    protected SocketMessage createMessageForMotionWithinRange(String actorId) {
        Motion motionWithinRange = createBaseMotion();
        PlayerMotion playerMotion = new PlayerMotion();
        playerMotion.setMotion(motionWithinRange);
        playerMotion.setActorId(actorId);

        SocketMessage playerMessageWithinRange = new SocketMessage();
        playerMessageWithinRange.setPlayerMotion(playerMotion);
        playerMessageWithinRange.setActorId(actorId);
        playerMessageWithinRange.setUpdateType(MessageType.PLAYER_MOTION.getType());

        return playerMessageWithinRange;
    }

    protected SocketMessage createMessageForSessionParams(boolean isPlayer, String actorId) {
        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setUpdateType(MessageType.SET_SESSION_ID.getType());
        if (isPlayer) {
            socketMessage.setActorId(actorId);
        } else {
            socketMessage.setServerName(actorId);
        }

        return socketMessage;
    }

    protected SocketMessage createMobMessageForMotionWithinRange(String actorId) {
        Motion motionWithinRange = createBaseMotion();

        Monster mob = new Monster();
        mob.setActorId(actorId);
        mob.setMobId(actorId);
        mob.setMotion(motionWithinRange);

        SocketMessage msg = new SocketMessage();
        msg.setMobId(actorId);
        msg.setActorId(actorId);
        msg.setMonster(mob);
        msg.setServerName(MOB_SERVER_NAME);

        return msg;
    }

    protected SocketMessage createMobMessageForMotionOutOfRange(String actorId) {
        SocketMessage msg = createMobMessageForMotionWithinRange(actorId);

        msg.getMonster().getMotion().setX(10_000);
        msg.getMonster().getMotion().setY(10_000);

        return msg;
    }

    protected List<SocketResponse> getSocketResponse(TestWebSocketClient client) {
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

    protected SocketResponse getLastMessageOfType(List<SocketResponse> responses, String type) {
        for (SocketResponse res : responses) {
            if (res.getMessageType().equals(type)) {
                return res;
            }
        }

        return null;
    }
}
