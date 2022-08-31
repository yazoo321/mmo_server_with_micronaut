//package server.motion.service.socket.v1.controller;
//
//
//import com.mongodb.lang.NonNull;
//import io.micronaut.context.BeanContext;
//import io.micronaut.context.annotation.Property;
//import io.micronaut.context.annotation.Requires;
//import io.micronaut.core.util.CollectionUtils;
//import io.micronaut.http.uri.UriBuilder;
//import io.micronaut.runtime.server.EmbeddedServer;
//import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
//import io.micronaut.websocket.WebSocketClient;
//import io.micronaut.websocket.annotation.ClientWebSocket;
//import io.micronaut.websocket.annotation.OnMessage;
//import org.junit.jupiter.api.Test;
//import org.reactivestreams.Publisher;
//
//import javax.inject.Inject;
//import javax.validation.constraints.NotBlank;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Deque;
//import java.util.List;
//import java.util.concurrent.ConcurrentLinkedDeque;
//
//
//@Property(name = "spec.name", value = "PlayerMotionSocketTest")
//@MicronautTest
//public class PlayerMotionSocketTest {
//
//    @Inject
//    BeanContext beanContext;
//
//    @Inject
//    EmbeddedServer embeddedServer;
//
//    private static final String MAP_1 = "map1";
//    private static final String MAP_2 = "map1";
//
//    private final String CHARACTER_1 = "character1";
//    private final String CHARACTER_2 = "character2";
//    private final String CHARACTER_3 = "character3";
//    private final String CHARACTER_4 = "character4";
//
//
//    @Requires(property = "spec.name", value = "PlayerMotionSocketTest")
//    @ClientWebSocket
//    static abstract class TestWebSocketClient implements AutoCloseable {
//
//        private final Deque<String> messageHistory = new ConcurrentLinkedDeque<>();
//
//        public String getLatestMessage() {
//            return messageHistory.peekLast();
//        }
//
//        public List<String> getMessagesChronologically() {
//            return new ArrayList<>(messageHistory);
//        }
//
//        @OnMessage
//        void onMessage(String message) {
//            messageHistory.add(message);
//        }
//
//        abstract void send(@NonNull @NotBlank String message);
//    }
//    private TestWebSocketClient createWebSocketClient(int port, String map, String playerName) {
//        WebSocketClient webSocketClient = beanContext.getBean(WebSocketClient.class);
//        URI uri = UriBuilder.of("ws://localhost")
//                .port(port)
//                .path("v1")
//                .path("player-motion")
//                .path("{map}")
//                .path("{playerName}")
//                .expand(CollectionUtils.mapOf("map", map, "username", playerName));
//        Publisher<TestWebSocketClient> client = webSocketClient.connect(TestWebSocketClient.class,  uri);
//        // requires to install reactor
//        return Flux.from(client).blockFirst();
//    }
//
//
//    @Test
//    void testWebsocketMotionUpdates() throws Exception {
//        TestWebSocketClient adam = createWebSocketClient(embeddedServer.getPort(), MAP_1, CHARACTER_1);
//        String expectedMsg;
//
//        await().until(() ->
//                Collections.singletonList(expectedMsg)
//                        .equals(adam.getMessagesChronologically()));
//
//        TestWebSocketClient anna = createWebSocketClient(embeddedServer.getPort(), MAP_1, CHARACTER_2);
//        await().until(() ->
//                Collections.singletonList("[anna] Joined Cats & Recreation!")
//                        .equals(anna.getMessagesChronologically()));
//        await().until(() ->
//                Arrays.asList("[adam] Joined Cats & Recreation!", "[anna] Joined Cats & Recreation!")
//                        .equals(adam.getMessagesChronologically()));
//
//        TestWebSocketClient ben = createWebSocketClient(embeddedServer.getPort(), "ben", "Fortran Tips & Tricks");
//        await().until(() ->
//                Collections.singletonList("[ben] Joined Fortran Tips & Tricks!")
//                        .equals(ben.getMessagesChronologically()));
//        TestWebSocketClient zach = createWebSocketClient(embeddedServer.getPort(), "zach", "all");
//        await().until(() ->
//                Collections.singletonList("[zach] Now making announcements!")
//                        .equals(zach.getMessagesChronologically()));
//        TestWebSocketClient cienna = createWebSocketClient(embeddedServer.getPort(), "cienna", "Fortran Tips & Tricks");
//        await().until(() ->
//                Collections.singletonList("[cienna] Joined Fortran Tips & Tricks!")
//                        .equals(cienna.getMessagesChronologically()));
//        await().until(() ->
//                Arrays.asList("[ben] Joined Fortran Tips & Tricks!", "[zach] Now making announcements!", "[cienna] Joined Fortran Tips & Tricks!") (10)
//                .equals(ben.getMessagesChronologically()));
//
//        // should broadcast message to all users inside the topic (11)
//        final String adamsGreeting = "Hello, everyone. It's another purrrfect day :-)";
//        final String expectedGreeting = "[adam] " + adamsGreeting;
//        adam.send(adamsGreeting);
//
//        //subscribed to "Cats & Recreation"
//        await().until(() ->
//                expectedGreeting.equals(adam.getLatestMessage()));
//
//        //subscribed to "Cats & Recreation"
//        await().until(() ->
//                expectedGreeting.equals(anna.getLatestMessage()));
//
//        //NOT subscribed to "Cats & Recreation"
//        assertNotEquals(expectedGreeting, ben.getLatestMessage());
//
//        //subscribed to the special "all" topic
//        await().until(() ->
//                expectedGreeting.equals(zach.getLatestMessage()));
//
//        //NOT subscribed to "Cats & Recreation"
//        assertNotEquals(expectedGreeting, cienna.getLatestMessage());
//
//        // should broadcast message when user disconnects from the chat (12)
//
//        anna.close();
//
//        String annaLeaving = "[anna] Leaving Cats & Recreation!";
//        await().until(() ->
//                annaLeaving.equals(adam.getLatestMessage()));
//
//        //subscribed to "Cats & Recreation"
//        assertEquals(annaLeaving, adam.getLatestMessage());
//
//        //Anna already left and therefore won't see the message about her leaving
//        assertNotEquals(annaLeaving, anna.getLatestMessage());
//
//        //NOT subscribed to "Cats & Recreation"
//        assertNotEquals(annaLeaving, ben.getLatestMessage());
//
//        //subscribed to the special "all" topic
//        assertEquals(annaLeaving, zach.getLatestMessage());
//
//        //NOT subscribed to "Cats & Recreation"
//        assertNotEquals(annaLeaving, cienna.getLatestMessage());
//
//        adam.close();
//        ben.close();
//        zach.close();
//        cienna.close();
//    }
//}
