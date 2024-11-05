package server.motion.socket.v2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micronaut.context.BeanContext;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.websocket.WebSocketClient;
import jakarta.inject.Inject;
import java.net.URI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import server.common.dto.Motion;
import server.monster.server_integration.model.Monster;
import server.motion.dto.MotionResult;
import server.motion.dto.PlayerMotion;
import server.motion.service.PlayerMotionService;
import server.util.PlayerMotionUtil;
import server.util.websocket.TestWebSocketClient;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MotionSocketV2Test {

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

    private TestWebSocketClient createWebSocketClient(int port, String map, String actorId) {
        WebSocketClient webSocketClient = beanContext.createBean(WebSocketClient.class);
        URI uri =
                UriBuilder.of("ws://localhost")
                        .port(port)
                        .path("v2")
                        .path("actor-updates")
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

    private MotionResult getMotionResult(TestWebSocketClient client) {
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
                && motion.getActorId().equals(expectedPlayerMotion.getActorId())
                && motion.getIsOnline().equals(expectedPlayerMotion.getIsOnline());
    }

    private boolean mobMotionMatches(MotionResult motionResult, Monster expectedMob) {
        if (motionResult.getMonster() == null) {
            return false;
        }

        Monster mob = motionResult.getMonster();

        return mob.getActorId().equals(expectedMob.getActorId())
                && mob.getMotion().equals(expectedMob.getMotion());
    }
}
