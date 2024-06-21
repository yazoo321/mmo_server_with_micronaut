package server.socket.service;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.session.SessionParamHelper;
import server.session.cache.UdpSessionCache;
import server.socket.model.SocketResponse;
import server.socket.model.UdpAddressHolder;
import server.socket.v1.CommunicationSocket;
import server.socket.v2.UDPServer;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.function.Predicate;

@Slf4j
@Singleton
public class UdpClientUpdateService extends ClientUpdatesService  {

    @Inject SocketProcessOutgoingService socketService;

    @Inject
    SessionParamHelper sessionParamHelper;

    @Inject
    UDPServer udpServer;

    @Inject
    UdpSessionCache sessionCache;

    private void sendWithFilter(Predicate<WebSocketSession> filter, SocketResponse message) {
        socketService.getLiveSessions().values()
                .parallelStream()
                .filter(filter)
                .forEach(s -> {
                    String id = SessionParamHelper.getIsPlayer(s) ?
                            SessionParamHelper.getActorId(s) : SessionParamHelper.getServerName(s);

                    try {
                        UdpAddressHolder addressHolder = sessionCache.fetchUdpSession(id);
                        udpServer.send(message, InetAddress.getByName(addressHolder.getHost()), addressHolder.getPort());
                    } catch (UnknownHostException e) {
                        log.error("Error sending UDP message, {}", e.getMessage());
                    }
                });

    }

    public void sendUpdateToListeningPlayers(SocketResponse message, String actorId) {
        sendWithFilter(sessionIsPlayerAndListensToActor(actorId), message);
    }

    public void sendUpdateToListening(SocketResponse message, String actorId) {
        // this is to send message to both, players and mobs, but excluding self.
        sendWithFilter(sessionListensToActorId(actorId), message);
    }

    public void sendUpdateToListeningIncludingServer(SocketResponse message, String actorId) {
        // this is to send message to both, players and mobs, but excluding self.
        sendWithFilter(sessionListensToActorIdWithServer(actorId), message);
    }

    public void sendUpdateToListeningIncludingSelf(SocketResponse message, String actorId) {
        // send message to anyone subscribed to this actor
        sendWithFilter(sessionListensToActorsOrIsTheActor(actorId), message);
    }


}
