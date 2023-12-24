package server.combat.combatInterface;

import io.micronaut.websocket.WebSocketSession;
import server.attribute.stats.model.Stats;
import server.combat.model.CombatRequest;

public interface CombatInterface {

    void requestAttack(WebSocketSession session, CombatRequest combatRequest);

    void requestStopAttack(String actorId);

    void tryAttack(WebSocketSession session, Stats target, boolean isMainHand);
}
