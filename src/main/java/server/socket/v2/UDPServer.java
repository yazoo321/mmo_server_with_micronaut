package server.socket.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.net.*;

import lombok.extern.slf4j.Slf4j;
import server.socket.model.SocketMessage;
import server.socket.model.SocketResponse;
import server.socket.service.SocketProcessOutgoingService;

@Slf4j
@Singleton
public class UDPServer {

    private static final int UDP_PORT = 9876;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectReader reader = mapper.reader();
    private static final ObjectWriter writer = mapper.writer();

    private DatagramSocket socket = new DatagramSocket();

    // TODO: put to config?
    private static final int RETRIES = 5;
    private int attempts = 0;


    @Inject SocketProcessOutgoingService socketProcessOutgoingService;

    public UDPServer() throws SocketException {
        Thread serverThread =
                new Thread(
                        () -> {
                            try {
                                startServer();
                            } catch (Exception e) {
                                log.error(e.getMessage());
                            }
                        });
        serverThread.start();
    }

    public void send(SocketResponse message, InetAddress address, Integer port) {
        try {
            byte[] data = writer.writeValueAsBytes(message);
            DatagramPacket packet =
                    new DatagramPacket(
                            data, data.length, address, port);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startServer() {
        try {
            DatagramSocket socket = new DatagramSocket(UDP_PORT);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                SocketMessage message = reader.readValue(packet.getData(), SocketMessage.class);

                String actorId = getActorId(message);

                if (actorId == null) {
                    log.error("UDP message did not contain actor ID, {}", message);

                    continue;
                }

                socketProcessOutgoingService.processUDPMessage(message);
                log.info("Message received! {}", message);
            }
        } catch (Exception e) {
            log.error("Server failed with stacktrace: {}", e.getMessage());
            e.printStackTrace();
            attempts++;
            if (attempts < RETRIES) {
                startServer();
            }
        }
    }

    private String getActorId(SocketMessage message) {
        if (message.getActorId() != null) {
            return message.getActorId();
        }

        if (message.getPlayerMotion() != null && message.getPlayerMotion().getActorId() != null
                && !message.getPlayerMotion().getActorId().isBlank()) {
            return message.getPlayerMotion().getActorId();
        }

        if (message.getMonster() != null && message.getMonster().getActorId() != null
                && !message.getMonster().getActorId().isBlank()) {
            return message.getMonster().getActorId();
        }

        return null;
    }

}
