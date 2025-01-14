package server.socket.service.integrations.status;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.repository.StatusRepository;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class StatusSocketIntegration {

    @Inject
    SocketResponseSubscriber socketResponseSubscriber;

    @Inject
    StatusRepository statusRepository;

    public void handleFetchActorStatus(String actorId, WebSocketSession session) {
        // Fetching using repository to avoid circular dependency
        statusRepository.getActorStatuses(actorId)
                .doOnSuccess(status -> {
                    status.setAdd(true);
                    sendStatus(status, session);
                })
                .doOnError(err-> log.error(err.getMessage()))
                .subscribe();
    }

    public void sendStatus(ActorStatus status, WebSocketSession session) {
        SocketResponse socketResponse =
                SocketResponse.builder()
                        .messageType(SocketResponseType.STATUS_UPDATE.getType())
                        .actorStatus(status)
                        .build();

        session.send(socketResponse).subscribe(socketResponseSubscriber);
    }
}
