package server.actionbar.service;

import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import server.actionbar.model.ActionbarContent;
import server.actionbar.model.ActorActionbar;
import server.actionbar.repository.ActionbarRepository;
import server.session.SessionParamHelper;
import server.socket.model.SocketResponse;
import server.socket.model.SocketResponseSubscriber;
import server.socket.model.SocketResponseType;

@Slf4j
@Singleton
public class ActionbarService {

    @Inject ActionbarRepository actionbarRepository;

    @Inject SocketResponseSubscriber socketResponseSubscriber;

    public void getActorActionbar(WebSocketSession session) {
        // this is only for players
        String actorId = SessionParamHelper.getActorId(session);
        actionbarRepository
                .getActorActionbar(actorId)
                .doOnSuccess(
                        actionbar -> {
                            SocketResponse response = new SocketResponse();
                            response.setMessageType(SocketResponseType.UPDATE_ACTIONBAR.getType());
                            response.setActionbarList(actionbar);

                            session.send(response).subscribe(socketResponseSubscriber);
                        })
                .doOnError(
                        err ->
                                log.error(
                                        "Failed to get actionbar for {}, {}",
                                        actorId,
                                        err.getMessage()))
                .subscribe();
    }

    public void updateActionbarItem(ActorActionbar actorActionbar) {
        ActionbarContent content = actorActionbar.getActionbarContent();

        if ((content.getItemId() == null || content.getItemId().isBlank())
                && (content.getSkillId() == null || content.getSkillId().isBlank())) {
            actionbarRepository.deleteActorActionbar(
                    actorActionbar.getActorId(), actorActionbar.getActionbarId());
        } else {
            actionbarRepository.updateActorActionbar(actorActionbar);
        }
    }
}
