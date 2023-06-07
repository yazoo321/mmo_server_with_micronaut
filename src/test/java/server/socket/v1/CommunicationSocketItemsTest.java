package server.socket.v1;

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
import io.micronaut.websocket.WebSocketSession;
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
import server.common.dto.Location;
import server.common.dto.Location2D;
import server.common.dto.Motion;
import server.items.helper.ItemTestHelper;
import server.items.inventory.model.response.GenericInventoryData;
import server.items.model.Item;
import server.items.model.ItemInstance;
import server.items.types.ItemType;
import server.items.types.weapons.Weapon;
import server.monster.server_integration.model.Monster;
import server.motion.dto.PlayerMotion;
import server.motion.service.PlayerMotionService;
import server.socket.model.MessageType;
import server.socket.model.SocketMessage;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseType;
import server.util.PlayerMotionUtil;
import server.util.websocket.TestWebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

// This test is designed to test the items flow
// dropping items, picking items up, etc.
@MicronautTest(environments = "kafka")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "spec.name", value = "PlayerMotionSocketTest")
public class CommunicationSocketItemsTest {
    @Inject BeanContext beanContext;

    @Inject EmbeddedServer embeddedServer;

    @Inject PlayerMotionService playerMotionService;

    @Inject PlayerMotionUtil playerMotionUtil;

    @Inject ItemTestHelper itemTestHelper;

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

    private final int TIMEOUT = 5;

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
        itemTestHelper.deleteAllItemData();
    }

    private TestWebSocketClient createWebSocketClient(int port) {
        WebSocketClient webSocketClient = beanContext.getBean(WebSocketClient.class);
        URI uri =
                UriBuilder.of("ws://localhost")
                        .port(port)
                        .path("v1")
                        .path("communication-socket")
                        .build();

        Publisher<TestWebSocketClient> client =
                webSocketClient.connect(TestWebSocketClient.class, uri);
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
    void testWhenPlayerDropsItemItIsDropped() {
        playerMotionService.initializePlayerMotion(CHARACTER_1).blockingGet();
        playerMotionService.initializePlayerMotion(CHARACTER_2).blockingGet();

        TestWebSocketClient playerClient1 =
                createWebSocketClient(embeddedServer.getPort());
        TestWebSocketClient playerClient2 =
                createWebSocketClient(embeddedServer.getPort());

        prepareCharactersAndItems();

    }

    private SocketMessage dropRequestForCharacter(Location2D inventoryLocation, Location dropLocation) {
        SocketMessage message = new SocketMessage();
        message.setUpdateType(MessageType.DROP_ITEM.getType());
        GenericInventoryData genericInventoryData = new GenericInventoryData();
        genericInventoryData.setItemInventoryLocation(inventoryLocation);
        genericInventoryData.setLocation(dropLocation);

        message.setInventoryRequest(genericInventoryData);

        return message;
    }


    private void prepareCharactersAndItems() {
        itemTestHelper.prepareInventory(CHARACTER_1);
        itemTestHelper.prepareInventory(CHARACTER_2);

        Item item = itemTestHelper.createAndInsertItem(ItemType.WEAPON.getType());
        ItemInstance itemInstance = itemTestHelper.createItemInstanceFor(item, UUID.randomUUID().toString());

        itemTestHelper.addItemToInventory(CHARACTER_1, itemInstance);
    }

}
