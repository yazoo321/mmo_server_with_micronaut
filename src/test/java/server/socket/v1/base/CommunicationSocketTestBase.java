package server.socket.v1.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micronaut.context.BeanContext;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.websocket.WebSocketClient;
import jakarta.inject.Inject;
import java.net.URI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import server.common.dto.Motion;
import server.items.helper.ItemTestHelper;
import server.motion.service.PlayerMotionService;
import server.util.PlayerMotionUtil;

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
    void setup() {
        cleanup();
    }

    @AfterAll
    void tearDown() {
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
}
