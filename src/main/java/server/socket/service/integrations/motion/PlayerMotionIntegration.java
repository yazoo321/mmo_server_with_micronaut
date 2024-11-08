package server.socket.service.integrations.motion;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import server.attribute.stats.service.StatsService;
import server.attribute.status.service.StatusService;
import server.common.dto.Motion;
import server.motion.dto.PlayerMotion;
import server.motion.repository.ActorMotionRepository;
import server.motion.repository.RespawnPoints;
import server.session.SessionParamHelper;
import server.socket.model.SocketMessage;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;
import server.socket.service.integrations.status.StatusSocketIntegration;

import java.time.Instant;
import java.util.Map;

@Singleton
public class PlayerMotionIntegration {

    @Inject
    RespawnPoints respawnPoints;

    @Inject
    ActorMotionRepository actorMotionRepository;

    @Inject
    SocketResponseSubscriber socketResponseSubscriber;

    @Inject
    StatusSocketIntegration statusSocketIntegration;

    @Inject
    StatusService statusService;

    @Inject
    StatsService statsService;

    public void handlePlayerRespawn(SocketMessage message, WebSocketSession session) {
        String actorId = SessionParamHelper.getActorId(session);
        actorMotionRepository.fetchActorMotion(actorId)
                .doOnSuccess(playerMotion -> {
                    Motion motion = respawnPoints.getRespawnPointFor(playerMotion, message.getCustomData());
                    SocketResponse response = new SocketResponse();
                    response.setMessageType(SocketResponseType.FORCE_UPDATE_MOTION.getType());

                    PlayerMotion updatedPlayerMotion = new PlayerMotion();
                    updatedPlayerMotion.setActorId(actorId);
                    updatedPlayerMotion.setMotion(motion);
                    updatedPlayerMotion.setIsOnline(true);
                    updatedPlayerMotion.setUpdatedAt(Instant.now());

                    response.setPlayerMotion(Map.of(actorId, updatedPlayerMotion));
                    session.send(response).subscribe(socketResponseSubscriber);

                    actorMotionRepository.updateActorMotion(actorId, motion);
                    statusService.removeAllStatuses(actorId).doOnSuccess(status ->
                            statusSocketIntegration.sendStatus(status, session)).subscribe();
                    statsService.resetHPAndMP(actorId, 0.7, 0.7);
                })
                .subscribe();
    }

    public void setPlayerOnline() {

    }
}
