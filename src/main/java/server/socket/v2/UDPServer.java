package server.socket.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    // TODO: put to config?
    private static final int RETRIES = 10;
    private int attempts = 0;

    Map<Integer, DatagramSocket> openSockets = new HashMap<>();

    @Inject SocketProcessOutgoingService socketProcessOutgoingService;

    private void startServerOnPort(int port) {
        Thread serverThread =
                new Thread(
                        () -> {
                            log.info("initialized udp server thread");
                            try {
                                startServer(port);
                            } catch (Exception e) {
                                log.error(e.getMessage());
                            }
                        });
        serverThread.start();
    }
    public UDPServer() throws SocketException {
        log.info("Starting udp server");
        List.of(UDP_PORT, 5000, 5001, 5002, 5003, 5004, 5005, 5006, 5007, 5008, 5009, 5010).forEach(
                this::startServerOnPort
        );
    }

    public void send(SocketResponse message, InetAddress address, Integer port) {
        try {
            DatagramSocket socket = openSockets.get(port);
            byte[] data = writer.writeValueAsBytes(message);
            DatagramPacket packet =
                    new DatagramPacket(
                            data, data.length, address, port);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startServer(int port) {
        try {
            DatagramSocket socket = openSockets.get(port);
            if (socket != null) {
                socket.close();
            }
            socket = new DatagramSocket(port);

            openSockets.put(port, socket);

            byte[] buffer = new byte[1024];

            log.info("UDP server started");

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                processUDPMessage(packet);
            }
        } catch (BindException e) {
          log.error("Bind exception detected, another thread already using this");
        } catch (Exception e) {
            log.error("Server failed with stacktrace: {}", e.getMessage());

            e.printStackTrace();
            attempts++;
            if (attempts < RETRIES) {
                startServer(port);
            } else {
                log.error("Critical error, server failed too many times, shutting down");
            }
        }
    }

    private boolean processUDPMessage(DatagramPacket packet) throws IOException {
//        log.info("Received packet from address: {} with port: {}", packet.getAddress(), packet.getPort());
//        log.info("Socket address: {}", packet.getSocketAddress());

        SocketMessage message = reader.readValue(packet.getData(), SocketMessage.class);
//        log.info("Message received! {}", message);
        String actorId = getActorId(message);

        if (actorId == null) {
            log.error("UDP message did not contain actor ID, {}", message);
            return true;
        }

        socketProcessOutgoingService.processUDPMessage(message);
        return false;
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
