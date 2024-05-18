package server.socket.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.socket.model.SocketMessage;
import server.socket.service.SocketProcessOutgoingService;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Singleton
public class UDPServer {

    private static final int UDP_PORT = 9876;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectReader reader = mapper.reader();
    private static final ObjectWriter writer = mapper.writer();

    ConcurrentHashMap<String, WebSocketSession> validIps = new ConcurrentHashMap<>();

    @Inject
    SocketProcessOutgoingService socketProcessService;

    public UDPServer() {
        Thread serverThread = new Thread(() -> {
            try {
                startServer();
            } catch (Exception e) {
               log.error(e.getMessage());
            }
        });
        serverThread.start();
    }

    public void addValidIp(String ip, WebSocketSession session) {
        validIps.put(ip, session);
    }

    public void removeValidIp(String ip) {
        validIps.remove(ip);
    }

    public void send(SocketMessage message) {
        try {
            byte[] data = writer.writeValueAsBytes(message);
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("localhost"), UDP_PORT);
            DatagramSocket socket = new DatagramSocket();
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

                String hostAddress = packet.getAddress().getHostName();
                if (!validIps.contains(hostAddress)) {
                    log.error("UDP received message from {} which is not connected to websocket",
                            hostAddress);
                    continue;
                }

                SocketMessage message = reader.readValue(packet.getData(), SocketMessage.class);

                WebSocketSession session = validIps.get(hostAddress);

                socketProcessService.processMessage(message, session);
                log.info("Message received! {}", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
